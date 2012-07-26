package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A form for editing the properties of a new or existing Store object.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Dependent
@Templated
public class StoreForm extends Composite {

  @Inject private EntityManager em;

  // injecting this data binder causes automatic binding between
  // the properties of Store and the like-named @DataField members in this class
  // Example: property "store.name" tracks the value in the TextBox "name"
  @Inject private DataBinder<Store> storeBinder;

  @Inject @DataField private TextBox name;
  @Inject @DataField private Button saveButton;

  private Runnable afterSaveAction;

  /**
   * Returns the store instance that is permanently associated with this form.
   * The returned instance is bound to this store's fields: updates to the form
   * fields will cause matching updates in the returned object's state, and
   * vice-versa.
   *
   * @return the Store instance that is bound to the fields of this form.
   */
  public Store getStore() {
    return storeBinder.getModel();
  }

  @PostConstruct
  private void init() {

    // TODO (errai-ui): I want to get events with an annotated method (like GWT's @UiHandler)
    //                  rather than programmatically subscribing a listener
    saveButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        em.persist(storeBinder.getModel());
        em.flush();

        if (afterSaveAction != null) {
          afterSaveAction.run();
        }
        System.out.println("Click handler finished for " + storeBinder.getModel());
      }
    });
  }

  @PreDestroy
  void cleanup() {
    storeBinder.unbind();
  }

  public void setAfterSaveAction(Runnable afterSaveAction) {
    this.afterSaveAction = afterSaveAction;
  }
}