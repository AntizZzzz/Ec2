package com.jiangyifen.ec2.test;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table.CellStyleGenerator;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

public class Lc_ChangeRowDynamicallyTest extends VerticalLayout {
	
	private static final long serialVersionUID = -1272542959353187523L;

	public Lc_ChangeRowDynamicallyTest() {

		// Create the table with a caption.
		final Table table = new Table("This is my Table");

		// Define the names of columns.
		table.addContainerProperty("Name", String.class, null);
		table.addContainerProperty("Age", Integer.class, null);
		table.addContainerProperty("Gender", String.class, null);

		// Add a few items in the table.
		table.addItem(new Object[] { "Mary", 20, "男" }, 1);
		table.addItem(new Object[] { "Jay", 21, "男" }, 2);
		table.addItem(new Object[] { "Johin", 21, "女" }, 3);
		// table.setStyleName("green");
		// table.setCellStyleGenerator(new CellStyleGenerator() {
		//
		// @Override
		// public String getStyle(Object itemId, Object propertyId) {
		//
		// System.out.println("The getStyle function is invoked");
		// Item item = table.getItem(itemId);
		// String name = item.getItemProperty("Name").getValue()
		// .toString();
		//
		// if (name.equals("Jay")) {
		//
		// return "boldcontent";
		//
		// } else if (name.equals("Mary")) {
		//
		// return "highlight-red";
		//
		// } else {
		//
		// return null;
		// }
		//
		// }
		// });

		this.addComponent(table);
		Button button = new Button("变色");

		button.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 2510737013102226063L;

			@Override
			public void buttonClick(ClickEvent event) {
				System.out.println("The button is clicked");
				// table.setStyleName("green");
				table.setCellStyleGenerator(new CellStyleGenerator() {

					private static final long serialVersionUID = 1599350652585863423L;

					@Override
					public String getStyle(Object itemId, Object propertyId) {

						System.out.println("The getStyle function is invoked");
						Item item = table.getItem(itemId);
						String name = item.getItemProperty("Name").getValue()
								.toString();

						if (name.equals("Jay")) {

							return "boldcontent";

						} else if (name.equals("Mary")) {

							return "highlight-red";

						} else {

							return null;
						}

					}
				});
			}
		});

		this.addComponent(button);

		// TODO
		// Link link = new Link(null, new ExternalResource("aa"));
		// link.setCaption("I am a link");
		// link.aL
		//
		// link.addListener(RepaintRequestListener.class, this, "clickEvent");
		// this.addComponent(link);

		final Button button2 = new Button("www.baidu.com");
		button2.setStyleName(BaseTheme.BUTTON_LINK);
		button2.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -5107094725830401413L;

			@Override
			public void buttonClick(ClickEvent event) {
				button2.addStyleName("red");

			}
		});
		this.addComponent(button2);

	}

	public void clickEvent() {
		System.out.println("The clickEvent function is invoked");
	}

}
