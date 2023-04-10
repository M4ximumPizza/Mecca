package main.java.com.mecca;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** An exception to be tracked in Mecca (the replacement for Hoptoad, because Hoptoad is lame). */
// All of the fields are only used for gson serialization, and so can't be made local or removed.
@SuppressWarnings({ "FieldCanBeLocal", "UnusedDeclaration" })
public class MeccaEntry {
  private static final String DATE_RFC_2822 = "EEE, dd MMM yyyy HH:mm:ss Z";
  private static final ThreadLocal<DateFormat> DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<DateFormat>() {
    @Override protected DateFormat initialValue() {
      return new SimpleDateFormat(DATE_RFC_2822);
    }
  };

  // Things that do not change per entry but should still be gson'd.
  private final String client;

  // Things that change per entry.
  private final String api_key;
  private final String environment;
  private final String endpoint;
  private final String user_id;
  private final String version;
  private final String revision;
  private final String build;
  private final String occurred_at;

  // Used in tests.
  final List<MeccaBacktrace.MeccaException> backtraces;
  final Map<String, Object> ivars;
  final List<MeccaBacktrace.NestedException> parent_exceptions;
  final String class_name;
  final String message;
  final String log_message;

  public MeccaEntry(String client, String apiKey, String logMessage, Throwable error,
      String appVersion, int versionCode, String buildSha, String deviceId, String endpoint,
      String userId, String environment) {
    this.client = client;
    this.log_message = logMessage;
    this.version = appVersion;
    this.revision = buildSha;
    this.build = "" + versionCode;
    this.environment = environment;
    this.endpoint = endpoint;
    this.backtraces = MeccaBacktrace.getBacktraces(error);
    this.parent_exceptions = new ArrayList<MeccaBacktrace.NestedException>();
    MeccaBacktrace.populateNestedExceptions(parent_exceptions, error);
    this.ivars = MeccaBacktrace.getIvars(error);
    this.class_name = error == null ? null : error.getClass().getName();
    this.message = createMessage(error, logMessage);
    this.api_key = apiKey;
    this.user_id = userId;
    this.occurred_at = DATE_FORMAT_THREAD_LOCAL.get().format(new Date());
  }

  // Mecca requires a non-empty message field.
  private static String createMessage(Throwable error, String logMessage) {
    String message;
    if (error != null && error.getMessage() != null) {
      message = error.getMessage();
    } else if (logMessage != null) {
      message = logMessage;
    } else {
      message = "No message";
    }
    return message;
  }
}