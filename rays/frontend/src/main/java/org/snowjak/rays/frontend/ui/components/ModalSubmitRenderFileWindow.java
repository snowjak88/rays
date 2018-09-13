package org.snowjak.rays.frontend.ui.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.RenderTask;
import org.snowjak.rays.Settings;
import org.snowjak.rays.frontend.messages.backend.commands.RequestRenderCreationFromSingleJson;
import org.snowjak.rays.frontend.messages.frontend.RunInUIThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.util.MimeTypeUtils;

import com.google.common.eventbus.EventBus;
import com.google.common.io.FileBackedOutputStream;
import com.google.gson.JsonSyntaxException;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SpringComponent
@VaadinSessionScope
public class ModalSubmitRenderFileWindow extends Window {
	
	private static final long serialVersionUID = 5190866332552898618L;
	private static final Logger LOG = LoggerFactory.getLogger(ModalSubmitRenderFileWindow.class);
	
	private final FormLayout form = new FormLayout();
	
	@Autowired
	private EventBus bus;
	
	@Autowired
	private MessageSource messages;
	
	private FileBackedOutputStream os;
	
	@PostConstruct
	public void init() {
		
		center();
		setModal(true);
		setVisible(true);
		setClosable(true);
		
		os = new FileBackedOutputStream(8192);
		final var upload = new Upload(messages.getMessage("renderfile.form.upload", null, getLocale()), (fn, mt) -> os);
		upload.setAcceptMimeTypes(MimeTypeUtils.APPLICATION_JSON_VALUE + "," + MimeTypeUtils.TEXT_PLAIN_VALUE);
		
		final var errorLabel = new Label();
		errorLabel.addStyleName(ValoTheme.LABEL_FAILURE);
		errorLabel.setVisible(false);
		
		final var progressBar = new ProgressBar();
		progressBar.setCaption("Upload in progress ...");
		progressBar.setEnabled(false);
		
		upload.addStartedListener((e) -> {
			LOG.debug("Started upload.");
			bus.post(new RunInUIThread(() -> {
				upload.setEnabled(false);
				errorLabel.setVisible(false);
				progressBar.setEnabled(true);
				progressBar.setValue(0.0f);
			}));
		});
		
		upload.addProgressListener((currL, totL) -> {
			LOG.trace("Uploading ({} bytes / {} total)", currL, totL);
			if (totL < 0)
				bus.post(new RunInUIThread(() -> progressBar.setIndeterminate(true)));
			else {
				final var fraction = ((float) currL) / ((float) totL);
				bus.post(new RunInUIThread(() -> {
					progressBar.setIndeterminate(false);
					progressBar.setValue(fraction);
				}));
			}
		});
		
		upload.addFailedListener((e) -> {
			LOG.debug("Upload failed: {}: {}", e.getReason().getClass().getSimpleName(), e.getReason().getMessage());
			bus.post(new RunInUIThread(() -> {
				upload.setEnabled(true);
				progressBar.setEnabled(false);
				errorLabel.setValue(messages.getMessage("renderfile.error.uploadfailed",
						new Object[] { e.getReason().getClass().getSimpleName(), e.getReason().getLocalizedMessage() },
						getLocale()));
				errorLabel.setVisible(true);
			}));
		});
		
		upload.addSucceededListener((e) -> {
			LOG.debug("Upload succeeded ({} bytes).", e.getLength());
			bus.post(new RunInUIThread(() -> {
				upload.setEnabled(true);
				progressBar.setEnabled(false);
			}));
			
			try {
				
				final var json = readUploadedFile(os.asByteSource().openStream());
				createRenderFromSingleJson(json);
				close();
				
			} catch (IOException ioe) {
				
				LOG.debug("Could not read uploaded file successfully: {}: {}", ioe.getClass().getSimpleName(),
						ioe.getMessage());
				
				bus.post(new RunInUIThread(() -> {
					errorLabel.setValue(messages.getMessage("renderfile.error.readuploaded",
							new Object[] { ioe.getClass().getSimpleName(), ioe.getLocalizedMessage() }, getLocale()));
					errorLabel.setVisible(true);
				}));
				
			} catch (JsonSyntaxException jse) {
				
				LOG.debug("Could not parse uploaded file: {}: {}", jse.getClass().getSimpleName(), jse.getMessage());
				bus.post(new RunInUIThread(() -> {
					errorLabel.setValue(messages.getMessage("renderfile.error.parseuploaded",
							new Object[] { jse.getClass().getSimpleName(), jse.getLocalizedMessage() }, getLocale()));
					errorLabel.setVisible(true);
				}));
				
			}
		});
		
		form.addComponent(upload);
		form.addComponent(progressBar);
		form.addComponent(errorLabel);
		
		setContent(form);
	}
	
	@PreDestroy
	public void onDestroy() {
		
		try {
			
			os.close();
			
		} catch (IOException e) {
			LOG.error("Cannot close OutputStream.", e);
		}
	}
	
	private String readUploadedFile(InputStream uploadedStream) throws IOException {
		
		LOG.debug("Reading uploaded file ...");
		
		try (var ir = new BufferedReader(new InputStreamReader(uploadedStream))) {
			
			final var jsonBuffer = new StringBuffer();
			
			String line = null;
			while ((line = ir.readLine()) != null)
				jsonBuffer.append(line + System.lineSeparator());
			
			LOG.trace("Finished reading uploaded file.");
			return jsonBuffer.toString();
			
		}
	}
	
	private void createRenderFromSingleJson(String json) throws JsonSyntaxException {
		
		LOG.debug("Creating render from single JSON descriptor.");
		
		LOG.trace("Certifying JSON before proceeding.");
		Settings.getInstance().getGson().fromJson(json, RenderTask.class);
		
		LOG.trace("JSON certified, submitting request to create Render+Scene.");
		bus.post(new RequestRenderCreationFromSingleJson(json));
		
	}
	
}
