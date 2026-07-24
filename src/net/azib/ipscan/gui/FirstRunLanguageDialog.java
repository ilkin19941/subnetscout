/*
  This file is a part of SubNet Scout source.
  SubNet Scout is a fork of Angry IP Scanner, licensed under GPLv2.
 */
package net.azib.ipscan.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A friendly language picker, shown once on the very first launch,
 * before Labels/locale is initialized. Keeps its own tiny bilingual
 * texts (not routed through Labels, since the locale isn't known yet).
 */
public class FirstRunLanguageDialog {

	/** @return language tag chosen by the user, e.g. "en" or "ru" */
	public static String open() {
		var display = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
		// Намеренно без SWT.CLOSE — у окна не будет системной кнопки закрытия ("крестика").
		// Выбор языка обязателен для продолжения работы приложения.
		var shell = new Shell(display, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		shell.setText("SubNet Scout");
		shell.setImage(new Image(display, FirstRunLanguageDialog.class.getResourceAsStream("/images/icon.png")));

		var chosen = new boolean[] { false };
		// Страховка: даже без кнопки закрытия некоторые оконные менеджеры (Alt+F4,
		// закрытие через панель задач) всё равно шлют событие Close — блокируем его,
		// пока язык не выбран явным кликом по одной из карточек.
		shell.addListener(SWT.Close, event -> event.doit = chosen[0]);

		var layout = new GridLayout(1, false);
		layout.marginWidth = 30;
		layout.marginHeight = 26;
		layout.verticalSpacing = 20;
		shell.setLayout(layout);

		var titleFont = new Font(display, new FontData(getSystemFontName(display), 15, SWT.BOLD));
		var title = new Label(shell, SWT.CENTER);
		title.setText("Choose your language     \u2022     Выберите язык");
		title.setFont(titleFont);
		title.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

		var descFont = new Font(display, new FontData(getSystemFontName(display), 10, SWT.NORMAL));
		var description = new Label(shell, SWT.CENTER | SWT.WRAP);
		description.setText(
			"SubNet Scout is a network scanning tool — it helps you find devices, open ports\n" +
			"and hosts on your own local network. Free and open source. Your scan results stay\n" +
			"on this PC; the app only checks online for new versions, nothing else is sent.\n\n" +
			"SubNet Scout — это инструмент сканирования сети: помогает находить устройства,\n" +
			"открытые порты и хосты в вашей собственной локальной сети. Бесплатно, с открытым\n" +
			"исходным кодом. Результаты сканирования остаются на этом ПК; приложение лишь\n" +
			"проверяет наличие новых версий онлайн, больше ничего не отправляется."
		);
		description.setFont(descFont);
		description.setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
		var descData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		descData.widthHint = 420;
		description.setLayoutData(descData);

		var cardsRow = new Composite(shell, SWT.NONE);
		var cardsLayout = new GridLayout(2, true);
		cardsLayout.horizontalSpacing = 24;
		cardsRow.setLayout(cardsLayout);
		cardsRow.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		var result = new String[] { "en" }; // default fallback

		var enCard = createLanguageCard(cardsRow, display, "/images/flags/en.png", "English", () -> {
			result[0] = "en";
			chosen[0] = true;
			shell.close();
		});
		var ruCard = createLanguageCard(cardsRow, display, "/images/flags/ru.png", "Русский", () -> {
			result[0] = "ru";
			chosen[0] = true;
			shell.close();
		});

		var hint = new Label(shell, SWT.CENTER);
		hint.setText("You can change this later in Preferences");
		hint.setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
		hint.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

		shell.pack();
		// центрируем на экране, т.к. родительского окна ещё нет
		var screen = display.getPrimaryMonitor().getBounds();
		var size = shell.getBounds();
		shell.setLocation(screen.x + (screen.width - size.width) / 2, screen.y + (screen.height - size.height) / 2);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		titleFont.dispose();
		descFont.dispose();

		return result[0];
	}

	private interface Choice { void select(); }

	private static Composite createLanguageCard(Composite parent, Display display, String flagResource, String label, Choice onSelect) {
		var card = new Composite(parent, SWT.BORDER);
		var layout = new GridLayout(1, false);
		layout.marginWidth = 18;
		layout.marginHeight = 16;
		layout.verticalSpacing = 10;
		card.setLayout(layout);
		card.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		card.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		var flagImage = new Image(display, FirstRunLanguageDialog.class.getResourceAsStream(flagResource));
		var flagLabel = new Label(card, SWT.NONE);
		flagLabel.setImage(flagImage);
		flagLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

		var textLabel = new Label(card, SWT.CENTER);
		textLabel.setText(label);
		var font = new Font(display, new FontData(getSystemFontName(display), 12, SWT.BOLD));
		textLabel.setFont(font);
		textLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

		// hover-подсветка и клик по всей карточке (не только по картинке/тексту)
		var hand = new Cursor(display, SWT.CURSOR_HAND);
		var normalBg = display.getSystemColor(SWT.COLOR_WHITE);
		var hoverBg = new Color(display, 235, 244, 255);

		var mouseHandlers = new MouseAdapter() {
			@Override public void mouseUp(MouseEvent e) { onSelect.select(); }
		};
		var enterExit = new org.eclipse.swt.events.MouseTrackAdapter() {
			@Override public void mouseEnter(MouseEvent e) { card.setBackground(hoverBg); }
			@Override public void mouseExit(MouseEvent e) { card.setBackground(normalBg); }
		};

		card.setCursor(hand);
		card.addMouseListener(mouseHandlers);
		card.addMouseTrackListener(enterExit);
		flagLabel.setCursor(hand);
		flagLabel.addMouseListener(mouseHandlers);
		flagLabel.addMouseTrackListener(enterExit);
		textLabel.setCursor(hand);
		textLabel.addMouseListener(mouseHandlers);
		textLabel.addMouseTrackListener(enterExit);

		card.addDisposeListener(e -> { flagImage.dispose(); font.dispose(); hand.dispose(); hoverBg.dispose(); });

		return card;
	}

	private static String getSystemFontName(Display display) {
		return display.getSystemFont().getFontData()[0].getName();
	}
}
