package org.cynic.ltg_export.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.synedra.validatorfx.Validator;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cynic.ltg_export.Configuration.ExportConfiguration;
import org.cynic.ltg_export.Configuration.ExportConfiguration.ReportExportConfiguration;
import org.cynic.ltg_export.Constants;
import org.cynic.ltg_export.Constants.Template;
import org.cynic.ltg_export.domain.ApplicationException;
import org.cynic.ltg_export.domain.holder.ApplicationContextHolder;
import org.cynic.ltg_export.domain.model.CredentialsModel;
import org.cynic.ltg_export.domain.model.FilterModel;
import org.cynic.ltg_export.function.ThrowingFunction;
import org.cynic.ltg_export.service.ExportService;
import org.cynic.ltg_export.service.WebService;
import org.cynic.ltg_export.ui.util.AlertUtils;

import java.io.File;
import java.net.URL;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

public class ApplicationForm extends Application implements Initializable {

  @FXML
  public PasswordField username;
  @FXML
  public PasswordField password;
  @FXML
  public CheckBox rememberMe;


  @FXML
  private Button browse;
  @FXML
  private TextField file;
  @FXML
  public TextField paymentPeriod;
  @FXML
  public DatePicker changeDateFrom;
  @FXML
  public DatePicker changeDateTo;
  @FXML
  public TextField systemNumber;
  @FXML
  public DatePicker sendDate;
  @FXML
  public DatePicker departureDateFrom;
  @FXML
  public DatePicker departureDateTo;
  @FXML
  public DatePicker arrivalDateFrom;
  @FXML
  public DatePicker arrivalDateTo;
  @FXML
  public TextField number;
  @FXML
  public Button export;
  @FXML
  public ChoiceBox<String> name;
  @FXML
  public Pane modal;

  private static final Validator VALIDATOR = new Validator();
  private static final Preferences PREFERENCES = Preferences.userNodeForPackage(ApplicationForm.class);


  @Override
  public void start(Stage primaryStage) {
    load(() -> primaryStage, () -> Constants.LOCALE);
  }


  private void load(Supplier<Stage> stageSupplier, Supplier<Locale> localeSupplier) {
    ResourceBundle bundle = ResourceBundle.getBundle("messages/message", localeSupplier.get());
    String name = ClassUtils.getSimpleName(ApplicationForm.class);
    Optional.of(getClass())
      .map(it -> it.getResource(Template.JXML_PATH.template(name)))
      .<Parent>map(ThrowingFunction.withTry(
        it -> FXMLLoader.load(it, bundle),
        e -> new ApplicationException("error.jxml.load", Map.entry("message", ExceptionUtils.getRootCauseMessage(e)))
      ))
      .map(Scene::new)
      .ifPresentOrElse(
        it -> {
          Stage stage = stageSupplier.get();
          stage.setResizable(Boolean.FALSE);
          stage.setTitle("LTG Export");
          stage.setScene(it);
          stage.show();
        },
        () -> {
          throw new ApplicationException("error.jxml.not-found", Map.entry("name", name));
        }
      );
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Set<ReportExportConfiguration> configuration = ApplicationContextHolder.getBean(ExportConfiguration.class);

    name.getItems().setAll(configuration.stream().map(ReportExportConfiguration::name).toList());
    username.setText(PREFERENCES.get(username.getId(), StringUtils.EMPTY));
    password.setText(PREFERENCES.get(password.getId(), StringUtils.EMPTY));
    file.setText(PREFERENCES.get(file.getId(), StringUtils.EMPTY));
    rememberMe.setSelected(
      StringUtils.isNotBlank(PREFERENCES.get(username.getId(), StringUtils.EMPTY)) &&
        StringUtils.isNotBlank(PREFERENCES.get(password.getId(), StringUtils.EMPTY)) &&
        StringUtils.isNotBlank(PREFERENCES.get(file.getId(), StringUtils.EMPTY))

    );

    VALIDATOR.createCheck()
      .withMethod(context -> Optional.of(name)
        .filter(it -> Objects.isNull(it.getValue()))
        .ifPresent(it -> context.error("!")))
      .decorates(name);

    Stream.of(
        username,
        password,
        paymentPeriod,
        file
      )
      .forEach(field -> {
        VALIDATOR.createCheck()
          .withMethod(context -> Optional.of(field)
            .filter(it -> StringUtils.isBlank(it.getText()))
            .ifPresent(it -> context.error("!")))
          .decorates(field);
      });
  }


  @FXML
  public void onExportAction() {
    try {
      modal.setVisible(Boolean.TRUE);

      Optional.of(rememberMe)
        .filter(CheckBox::isSelected)
        .ifPresentOrElse(
          ignored -> storeCredentials(),
          this::removeCredentials
        );

      Optional.of(VALIDATOR.validate())
        .filter(Boolean.TRUE::equals)
        .map(it -> export())
        .ifPresent(it -> AlertUtils.info("Export completed", "Export saved in " + it));
    } catch (ApplicationException e) {
      AlertUtils.error(e);
    } catch (Throwable e) {
      AlertUtils.error(e);
    } finally {
      modal.setVisible(Boolean.FALSE);
    }
  }

  private void removeCredentials() {
    PREFERENCES.remove(username.getId());
    PREFERENCES.remove(password.getId());
    PREFERENCES.remove(rememberMe.getId());
    PREFERENCES.remove(file.getId());
  }

  private void storeCredentials() {
    PREFERENCES.put(username.getId(), username.getText());
    PREFERENCES.put(password.getId(), password.getText());
    PREFERENCES.putBoolean(rememberMe.getId(), rememberMe.isSelected());
    PREFERENCES.put(file.getId(), file.getText());
  }

  public void onBroseAction(ActionEvent event) {
    Optional.of(new DirectoryChooser())
      .map(it -> it.showDialog(Button.class.cast(event.getSource()).getScene().getWindow()))
      .map(File::getAbsolutePath)
      .ifPresent(it -> file.setText(it));
  }

  private String export() {
    WebService webService = ApplicationContextHolder.getBean(WebService.class);
    ExportService exportService = ApplicationContextHolder.getBean(ExportService.class);
    BiConsumer<String, byte[]> writer = ApplicationContextHolder.getBean(BiConsumer.class);
    Clock clock = ApplicationContextHolder.getBean(Clock.class);

    String exportName = name.getValue();

    String fileName =

      String.format(
        Constants.LOCALE,
        "%s%s%s_%s.csv",
        file.getText(),
        File.separator,
        exportName,
        LocalDateTime.now(clock).format(Constants.YYYY_MM_DD_HH_MM_SS));

    List<JsonNode> items = webService.export(
      new CredentialsModel(
        username.getText(),
        password.getText()
      ),
      exportName,
      new FilterModel(
        paymentPeriod.getText(),
        changeDateFrom.getValue(),
        changeDateTo.getValue(),
        systemNumber.getText(),
        sendDate.getValue(),
        departureDateFrom.getValue(),
        departureDateTo.getValue(),
        arrivalDateFrom.getValue(),
        arrivalDateTo.getValue(),
        number.getText()
      )
    );

    byte[] report = exportService.export(exportName, items).get();

    writer.accept(fileName, report);

    return fileName;
  }

}
