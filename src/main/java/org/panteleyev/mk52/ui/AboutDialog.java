/*
 Copyright © 2024-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.ui;

import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.TimeZone;

import static java.util.ResourceBundle.getBundle;
import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.mk52.ui.Mk52Controller.APP_TITLE;
import static org.panteleyev.mk52.ui.Styles.BIG_SPACING;
import static org.panteleyev.mk52.ui.Styles.SMALL_SPACING;
import static org.panteleyev.mk52.ui.Styles.STYLE_ABOUT_LABEL;

public class AboutDialog extends BaseDialog<Object> {
    private static final ResourceBundle BUILD_INFO_BUNDLE = getBundle("buildInfo");

    private record BuildInformation(String version, String timestamp) {
        static BuildInformation load() {
            return new BuildInformation(
                    BUILD_INFO_BUNDLE.getString("version"),
                    BUILD_INFO_BUNDLE.getString("timestamp")
            );
        }
    }

    private static final String YEAR = Integer.toString(LocalDate.now().getYear());

    private static final String RUNTIME = System.getProperty("java.vm.version") + " " + System.getProperty("os.arch");
    private static final String VM = System.getProperty("java.vm.name") + " by " + System.getProperty("java.vm.vendor");
    private static final BuildInformation BUILD = BuildInformation.load();

    private static final ZoneId LOCAL_TIME_ZONE = TimeZone.getDefault().toZoneId();
    private static final DateTimeFormatter TIMESTAMP_PARSER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssVV");
    private static final DateTimeFormatter LOCAL_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public AboutDialog(Controller owner) {
        super(null, "/about-dialog.css");

        setHeaderText("MК-52");
        setGraphic(new ImageView(Picture.ICON.getImage()));

        setTitle("О программе");

        var aboutLabel = label(APP_TITLE + " " + BUILD.version());
        aboutLabel.getStyleClass().add(STYLE_ABOUT_LABEL);

        var timestamp = ZonedDateTime.parse(BUILD.timestamp(), TIMESTAMP_PARSER)
                .withZoneSameInstant(LOCAL_TIME_ZONE);

        var vBox = vBox(BIG_SPACING,
                vBox(SMALL_SPACING,
                        aboutLabel,
                        label("Built on " + LOCAL_FORMATTER.format(timestamp))
                ),
                vBox(SMALL_SPACING,
                        label("Runtime version: " + RUNTIME),
                        label("VM: " + VM)
                ),
                vBox(SMALL_SPACING,
                        label("Copyright © " + YEAR + " Petr Panteleyev")
                )
        );

        getDialogPane().setContent(vBox);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK);

    }
}
