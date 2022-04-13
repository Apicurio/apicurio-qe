package apicurito.tests.utils.slenide;

import static org.junit.Assert.fail;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.text;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import apicurito.tests.configuration.CustomWebDriverProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImportExportUtils {
    public static File exportAPIUtil(String format) {

        CommonUtils.getAppRoot().$$("button")
            .filter(attribute("class", "btn btn-lg btn-primary dropdown-toggle btn-save")).first().click();
        CommonUtils.getAppRoot().$$("a").filter(text("Save as " + format.toUpperCase())).first().click();

        String filePath = CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + "openapi-spec." + format.toLowerCase();

        // wait for download
        try {
            Thread.sleep(5000);
        } catch (InterruptedException exception) {
            log.warn("Wait for download failed. File does not have to be downloaded.");
        }
        return new File(filePath);
    }

    public static void importAPI(File file) {
        CommonUtils.getAppRoot().$("#load-file").uploadFile(file);
    }

    public static File exportFuseCamelProject() {
        CommonUtils.getAppRoot().$$("button")
            .filter(attribute("class", "btn btn-lg btn-default dropdown-toggle")).first().click();
        CommonUtils.getAppRoot().$$("a").filter(text("Fuse Camel Project")).first().click();

        String filePath = CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + "camel-project.zip";

        // wait for download
        try {
            Thread.sleep(5000);
        } catch (InterruptedException exception) {
            log.warn("Wait for download failed. The project is not generated.");
        }
        if (CommonUtils.getAppRoot().$$("div").filter(attribute("class", "alert-global alert alert-danger alert-dismissable shown")).first()
            .isDisplayed()) {
            fail("Failed to generate the project.");
        }

        return new File(filePath);
    }

    public static void decompressZip(File source, File destination) {
        log.info("Decompressing ZIP file");
        try (ZipInputStream i = new ZipInputStream(new FileInputStream(source))) {
            ZipEntry entry;
            while ((entry = i.getNextEntry()) != null) {
                final String name = destination.getAbsolutePath() + File.separator + entry.getName();
                final File file = new File(name);
                if (entry.isDirectory()) {
                    if (!file.isDirectory() && !file.mkdirs()) {
                        throw new IOException("Failed to create directory " + file);
                    }
                } else {
                    final File parent = file.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (OutputStream o = Files.newOutputStream(file.toPath())) {
                        IOUtils.copy(i, o);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Can't decompress EnMasse.", e);
        }
    }
}
