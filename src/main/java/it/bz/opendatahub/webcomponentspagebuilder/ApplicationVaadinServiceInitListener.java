package it.bz.opendatahub.webcomponentspagebuilder;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.spring.annotation.SpringComponent;

/**
 * Component for adapting and configuring {@link UI} instances as they are
 * created by the servlet.
 * 
 * @author danielrampanelli
 */
@SpringComponent
public class ApplicationVaadinServiceInitListener implements VaadinServiceInitListener {

	private static final long serialVersionUID = 4531922505696048500L;

	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.getSource().addUIInitListener(uiEvent -> {
			final UI ui = uiEvent.getUI();

			// TODO configure offline banner/message

			// https://github.com/vaadin/flow/issues/5864
			ui.getPushConfiguration().setPushMode(PushMode.MANUAL);
		});
	}

}