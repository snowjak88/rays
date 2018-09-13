package org.snowjak.rays.frontend.ui.components;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.messages.frontend.AddWindowRequest;
import org.snowjak.rays.frontend.messages.frontend.RunInUIThread;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogin;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogout;
import org.snowjak.rays.frontend.security.SecurityOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.Navigator;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.themes.ValoTheme;

@SpringComponent
@VaadinSessionScope
public class MainMenuBar extends HorizontalLayout {
	
	private static final long serialVersionUID = 1635170014857768776L;
	private static final Logger LOG = LoggerFactory.getLogger(MainMenuBar.class);
	
	private final MenuBar mainMenuBar = new MenuBar();
	private final MenuBar logInOutBar = new MenuBar();
	private MenuBar.MenuItem logInOutItem;
	
	@Autowired
	private EventBus bus;
	
	@Autowired
	private MessageSource messages;
	
	@Autowired
	@Lazy
	private Navigator navigator;
	
	@Autowired
	private SecurityOperations security;
	
	@Autowired
	private ModalLoginWindow loginWindow;
	
	@Autowired
	private ModalSubmitRenderFileWindow submitRenderFileWindow;
	
	public MainMenuBar() {
		
		super();
	}
	
	@PostConstruct
	public void init() {
		
		setWidth(100, Unit.PERCENTAGE);
		setSpacing(false);
		setMargin(false);
		
		final var rendersSubMenu = mainMenuBar.addItem(messages.getMessage("mainmenu.rendersmenu", null, getLocale()),
				null, null);
		
		rendersSubMenu.addItem(messages.getMessage("mainmenu.rendersmenu.renderlist", null, getLocale()),
				(item) -> navigator.navigateTo(RenderList.NAME));
		rendersSubMenu.addItem(messages.getMessage("mainmenu.rendersmenu.creator", null, getLocale()),
				(item) -> navigator.navigateTo(ObjectCreator.NAME));
		
		rendersSubMenu.addSeparator();
		
		rendersSubMenu.addItem(messages.getMessage("mainmenu.rendersmenu.submitrenderfile", null, getLocale()),
				(item) -> bus.post(new AddWindowRequest(submitRenderFileWindow)));
		
		if (security.isAuthenticated()) {
			mainMenuBar.setEnabled(true);
			logInOutItem = logInOutBar.addItem(
					messages.getMessage("mainmenu.account.logout", null, LocaleContextHolder.getLocale()),
					VaadinIcons.SIGN_OUT, (item) -> security.doLogOut());
			
		} else {
			
			mainMenuBar.setEnabled(false);
			logInOutItem = logInOutBar.addItem(
					messages.getMessage("mainmenu.account.login", null, LocaleContextHolder.getLocale()),
					VaadinIcons.SIGN_IN, (item) -> bus.post(new AddWindowRequest(loginWindow)));
			
		}
		
		mainMenuBar.setWidth(100, Unit.PERCENTAGE);
		mainMenuBar.setStyleName(ValoTheme.MENUBAR_BORDERLESS);
		
		logInOutBar.setWidthUndefined();
		logInOutBar.setStyleName(ValoTheme.MENUBAR_BORDERLESS);
		
		addComponent(mainMenuBar);
		addComponent(logInOutBar);
		
		setExpandRatio(mainMenuBar, 1);
		setExpandRatio(logInOutBar, 0);
		
		bus.register(this);
	}
	
	@PreDestroy
	public void onDestroy() {
		
		bus.unregister(this);
	}
	
	@Subscribe
	public void onSuccessfulLoginEvent(SuccessfulLogin event) {
		
		bus.post(new RunInUIThread(() -> {
			LOG.trace("Detected authentication-success. Manipulating main-menu items.");
			
			mainMenuBar.setEnabled(true);
			
			logInOutItem.setText(messages.getMessage("mainmenu.account.logout", null, LocaleContextHolder.getLocale()));
			logInOutItem.setIcon(VaadinIcons.SIGN_OUT);
			logInOutItem.setCommand((item) -> security.doLogOut());
		}));
	}
	
	@Subscribe
	public void onSuccessfulLogoutEvent(SuccessfulLogout event) {
		
		bus.post(new RunInUIThread(() -> {
			LOG.trace("Detected log-out. Manipulating main-menu items.");
			
			mainMenuBar.setEnabled(false);
			
			logInOutItem.setText(messages.getMessage("mainmenu.account.login", null, LocaleContextHolder.getLocale()));
			logInOutItem.setIcon(VaadinIcons.SIGN_IN);
			logInOutItem.setCommand((item) -> bus.post(new AddWindowRequest(loginWindow)));
		}));
	}
	
}
