module twitter {
    requires javafx.web;

    requires org.seleniumhq.selenium.api;
    requires org.seleniumhq.selenium.remote_driver;
    requires org.seleniumhq.selenium.chrome_driver;
    requires org.seleniumhq.selenium.devtools_v131;
    requires org.seleniumhq.selenium.http;
    requires org.testng;
    requires dev.failsafe.core;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.jfoenix;
    requires static lombok;
    requires org.seleniumhq.selenium.support;
    requires org.seleniumhq.selenium.chromium_driver;
    requires org.seleniumhq.selenium.manager;
    requires com.google.gson;
    requires MaterialFX;
    requires fr.brouillard.oss.cssfx;  // Add other required JavaFX modules here.

    opens twitter.application to
            javafx.graphics, javafx.fxml, com.google.gson, org.seleniumhq.selenium.api;
    opens twitter.entity to com.google.gson, org.seleniumhq.selenium.api;
    opens twitter.scraper to com.google.gson, org.seleniumhq.selenium.api;
    opens twitter.algorithms to com.google.gson, org.seleniumhq.selenium.api;
    opens twitter.navigators to com.google.gson, org.seleniumhq.selenium.api;
    opens twitter.controller to com.google.gson, org.seleniumhq.selenium.api;
    opens twitter to com.google.gson, org.seleniumhq.selenium.api;

    exports twitter;
}