package com.gawkat.flashcard.client.card;

import com.gawkat.flashcard.client.login.Login;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Menu extends Composite {

  private VerticalPanel pWidget = new VerticalPanel();
  
  private Login wLogin = new Login();
  
  public Menu() {
    
    HorizontalPanel hp = new HorizontalPanel();

    hp.add(wLogin);
    
    pWidget.add(hp);
    
    initWidget(pWidget);
    
    pWidget.setStyleName("menu");

  }
  
}
