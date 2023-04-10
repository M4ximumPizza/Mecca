package test.java.com.mecca;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeccaEntryTest {

    private Gson gson = new Gson();
    private EntryFactory factory = new EntryFactory();
  
    @Test public void testNoException() throws Exception {
      final String message = "I LOVE TACOS";
      final MeccaEntry logEntry = factory.create(message, null);
      MeccaEntry deserialized = serializeAndDeserialize(logEntry);
      assertThat(deserialized.backtraces).isNull();
      assertThat(deserialized.ivars).isNull();
      assertThat(deserialized.log_message).isEqualTo(message);
      assertThat(deserialized.parent_exceptions).isEmpty();
      assertThat(deserialized.class_name).isNull();
    }
  
    private MeccaEntry serializeAndDeserialize(MeccaEntry logEntry) throws IOException {
      return gson.fromJson(gson.toJson(logEntry), MeccaEntry.class);
    }
  
    @Test public void testWithSimpleExceptionNoIvars() throws Exception {
      final String logMessage = "I LOVE TACOS";
      final Throwable exception = mock(Throwable.class);
      StackTraceElement s0 =
          new StackTraceElement("com.taco.Taco", "digest", "core-android/src/com/taco/Taco.java", 50);
      StackTraceElement s1 =
          new StackTraceElement("com.taco.Taco", "eat", "core-android/src/com/taco/Taco.java", 80);
      StackTraceElement s2 =
          new StackTraceElement("com.taco.Dude", "purchase", "core-android/src/com/taco/Dude.java",
              112);
      StackTraceElement[] myLittleStackTrace = new StackTraceElement[] {s0, s1, s2};
      final String message = "ExceptionMessage";
      when(exception.getMessage()).thenReturn(message);
      when(exception.getStackTrace()).thenReturn(myLittleStackTrace);
      final MeccaEntry logEntry = factory.create(logMessage, exception);
      MeccaEntry deserialized = serializeAndDeserialize(logEntry);
      assertThat(deserialized.backtraces).isNotEmpty();
      final MeccaBacktrace.MeccaException backtrace = deserialized.backtraces.get(0);
      assertThat(backtrace.name).isEqualTo(Thread.currentThread().getName());
      assertThat(backtrace.faulted).isEqualTo(true);
      List<MeccaBacktrace.StackElement> stackElements = backtrace.backtrace;
      assertBacktracesMatch(myLittleStackTrace, stackElements);
      assertThat(deserialized.ivars).isEmpty();
      assertThat(deserialized.log_message).isEqualTo(logMessage);
      assertThat(deserialized.message).isEqualTo(message);
      assertThat(deserialized.parent_exceptions).isEmpty();
      assertThat(deserialized.class_name).isEqualTo(exception.getClass().getName());
    }
  
    @Test public void testExceptionWithNoMessage() throws Exception {
      final Throwable exception = mock(Throwable.class);
  
      StackTraceElement s0 = new StackTraceElement("com.jake", "CantProgram",
          "core-android/src/com/jake/Brain.java", 50);
      StackTraceElement s1 = new StackTraceElement("com.jake", "IsDrunk",
          "core-android/src/com/jake/Status.java", 510);
      StackTraceElement[] stackTrace = { s0, s1 };
  
      when(exception.getMessage()).thenReturn(null);
      when(exception.getStackTrace()).thenReturn(stackTrace);
  
      String logMessage = "Jake can't program";
      final MeccaEntry logEntry = factory.create(logMessage, exception);
      MeccaEntry deserialized = serializeAndDeserialize(logEntry);
      assertThat(deserialized.message).isEqualTo(logMessage);
    }
  
    @Test public void testExceptionWithNoMessageOrLogMessage() throws Exception {
      final Throwable exception = mock(Throwable.class);
  
      StackTraceElement s0 = new StackTraceElement("com.jake", "CantProgram",
          "core-android/src/com/jake/Brain.java", 50);
      StackTraceElement s1 = new StackTraceElement("com.jake", "IsDrunk",
          "core-android/src/com/jake/Status.java", 510);
      StackTraceElement[] stackTrace = { s0, s1 };
  
      when(exception.getMessage()).thenReturn(null);
      when(exception.getStackTrace()).thenReturn(stackTrace);
  
      final MeccaEntry logEntry = factory.create(null, exception);
      MeccaEntry deserialized = serializeAndDeserialize(logEntry);
      assertThat(deserialized.message).isEqualTo("No message");
    }
  
    private void assertBacktracesMatch(StackTraceElement[] myLittleStackTrace,
        List<MeccaBacktrace.StackElement> stackElements) {
      for (int i = 0, stackElementsSize = stackElements.size(); i < stackElementsSize; i++) {
        MeccaBacktrace.StackElement stackElement = stackElements.get(i);
        StackTraceElement expected = myLittleStackTrace[i];
        assertThat(stackElement.file).isEqualTo(expected.getFileName());
        assertThat(stackElement.line).isEqualTo(expected.getLineNumber());
        assertThat(stackElement.symbol).isEqualTo(expected.getMethodName());
        assertThat(stackElement.class_name).isEqualTo(expected.getClassName());
      }
    }
  
    @Test public void testNestedExceptions() throws Exception {
      final String logMessage = "I LOVE TACOS";
      final Throwable nestedException = mock(Throwable.class);
      final Throwable doublyNestedException = mock(Throwable.class);
      final Throwable exception = mock(Throwable.class);
      StackTraceElement n0 = new StackTraceElement("com.taco.Burrito", "digest",
          "core-android/src/com/burrito/Burrito.java", 45);
      StackTraceElement n1 = new StackTraceElement("com.taco.Burrito", "eat",
          "core-android/src/com/burrito/Burrito.java", 10);
      StackTraceElement n2 =
          new StackTraceElement("com.taco.Dude", "purchase", "core-android/src/com/taco/Dude.java",
              65);
      StackTraceElement[] nestedStackTrace = new StackTraceElement[] {n0, n1, n2};
      StackTraceElement z0 =
          new StackTraceElement("com.taco.Dude", "wheresmycar", "core-android/src/com/taco/Dude.java",
              455);
      StackTraceElement z1 =
          new StackTraceElement("com.bro.Bro", "hollerback", "core-android/src/com/bro/Bro.java",
              105);
      StackTraceElement z2 =
          new StackTraceElement("com.taco.Dude", "holler", "core-android/src/com/taco/Dude.java",
              655);
      StackTraceElement[] doublyNestedStackTrace = new StackTraceElement[] {z0, z1, z2};
      StackTraceElement s0 =
          new StackTraceElement("com.taco.Taco", "digest", "core-android/src/com/taco/Taco.java", 50);
      StackTraceElement s1 =
          new StackTraceElement("com.taco.Taco", "eat", "core-android/src/com/taco/Taco.java", 80);
      StackTraceElement s2 =
          new StackTraceElement("com.taco.Dude", "purchase", "core-android/src/com/taco/Dude.java",
              112);
      StackTraceElement[] myLittleStackTrace = new StackTraceElement[] {s0, s1, s2};
      final String message = "ExceptionMessage";
      when(exception.getMessage()).thenReturn(message);
      when(exception.getStackTrace()).thenReturn(myLittleStackTrace);
      when(exception.getCause()).thenReturn(nestedException);
  
      final String nestedExceptionMessage = "NestedExceptionMessage";
      when(nestedException.getMessage()).thenReturn(nestedExceptionMessage);
      when(nestedException.getStackTrace()).thenReturn(nestedStackTrace);
      when(nestedException.getCause()).thenReturn(doublyNestedException);
  
      final String doublyNestedExceptionMessage = "DoublyNestedExceptionMessage";
      when(doublyNestedException.getMessage()).thenReturn(doublyNestedExceptionMessage);
      when(doublyNestedException.getStackTrace()).thenReturn(doublyNestedStackTrace);
  
      final MeccaEntry logEntry = factory.create(logMessage, exception);
      MeccaEntry deserialized = serializeAndDeserialize(logEntry);
      assertThat(deserialized.backtraces).isNotEmpty();
      MeccaBacktrace.MeccaException backtrace = deserialized.backtraces.get(0);
      assertThat(backtrace.name).isEqualTo(Thread.currentThread().getName());
      assertThat(backtrace.faulted).isEqualTo(true);
      List<MeccaBacktrace.StackElement> stackElements = backtrace.backtrace;
      assertBacktracesMatch(myLittleStackTrace, stackElements);
      assertThat(deserialized.ivars).isEmpty();
      assertThat(deserialized.log_message).isEqualTo(logMessage);
      assertThat(deserialized.message).isEqualTo(message);
      final List<MeccaBacktrace.NestedException> nestedExceptions = deserialized.parent_exceptions;
      assertThat(nestedExceptions).hasSize(2);
  
      final MeccaBacktrace.NestedException nested1 = nestedExceptions.get(0);
      assertThat(nested1.class_name).isEqualTo(nestedException.getClass().getName());
      assertThat(nested1.ivars).isEmpty();
      assertThat(nested1.message).isEqualTo(nestedExceptionMessage);
      backtrace = nested1.backtraces.get(0);
      assertThat(backtrace.name).isEqualTo(Thread.currentThread().getName());
      assertThat(backtrace.faulted).isEqualTo(true);
      assertBacktracesMatch(nestedStackTrace, backtrace.backtrace);
  
      final MeccaBacktrace.NestedException nested2 = nestedExceptions.get(1);
      assertThat(nested2.class_name).isEqualTo(doublyNestedException.getClass().getName());
      assertThat(nested2.ivars).isEmpty();
      assertThat(nested2.message).isEqualTo(doublyNestedExceptionMessage);
      backtrace = nested1.backtraces.get(0);
      assertThat(backtrace.name).isEqualTo(Thread.currentThread().getName());
      assertThat(backtrace.faulted).isEqualTo(true);
      assertBacktracesMatch(nestedStackTrace, backtrace.backtrace);
    }
  
    @Test public void testInfinitelyNestedExceptions() throws Exception {
      final String logMessage = "I LOVE TACOS";
      final Throwable nestedException = mock(Throwable.class);
      final Throwable doublyNestedException = mock(Throwable.class);
      final Throwable exception = mock(Throwable.class);
      StackTraceElement n0 = new StackTraceElement("com.taco.Burrito", "digest",
          "core-android/src/com/burrito/Burrito.java", 45);
      StackTraceElement n1 = new StackTraceElement("com.taco.Burrito", "eat",
          "core-android/src/com/burrito/Burrito.java", 10);
      StackTraceElement n2 =
          new StackTraceElement("com.taco.Dude", "purchase", "core-android/src/com/taco/Dude.java",
              65);
      StackTraceElement[] nestedStackTrace = new StackTraceElement[] {n0, n1, n2};
      StackTraceElement z0 =
          new StackTraceElement("com.taco.Dude", "wheresmycar", "core-android/src/com/taco/Dude.java",
              455);
      StackTraceElement z1 =
          new StackTraceElement("com.bro.Bro", "hollerback", "core-android/src/com/bro/Bro.java",
              105);
      StackTraceElement z2 =
          new StackTraceElement("com.taco.Dude", "holler", "core-android/src/com/taco/Dude.java",
              655);
      StackTraceElement[] doublyNestedStackTrace = new StackTraceElement[] {z0, z1, z2};
      StackTraceElement s0 =
          new StackTraceElement("com.taco.Taco", "digest", "core-android/src/com/taco/Taco.java", 50);
      StackTraceElement s1 =
          new StackTraceElement("com.taco.Taco", "eat", "core-android/src/com/taco/Taco.java", 80);
      StackTraceElement s2 =
          new StackTraceElement("com.taco.Dude", "purchase", "core-android/src/com/taco/Dude.java",
              112);
      StackTraceElement[] myLittleStackTrace = new StackTraceElement[] {s0, s1, s2};
      final String message = "ExceptionMessage";
      when(exception.getMessage()).thenReturn(message);
      when(exception.getStackTrace()).thenReturn(myLittleStackTrace);
      when(exception.getCause()).thenReturn(nestedException);
  
      final String nestedExceptionMessage = "NestedExceptionMessage";
      when(nestedException.getMessage()).thenReturn(nestedExceptionMessage);
      when(nestedException.getStackTrace()).thenReturn(nestedStackTrace);
      when(nestedException.getCause()).thenReturn(doublyNestedException);
  
      final String doublyNestedExceptionMessage = "DoublyNestedExceptionMessage";
      when(doublyNestedException.getMessage()).thenReturn(doublyNestedExceptionMessage);
      when(doublyNestedException.getStackTrace()).thenReturn(doublyNestedStackTrace);
      when(doublyNestedException.getCause()).thenReturn(doublyNestedException);
  
      final MeccaEntry logEntry = factory.create(logMessage, exception);
      MeccaEntry deserialized = serializeAndDeserialize(logEntry);
      assertThat(deserialized.backtraces).isNotEmpty();
      MeccaBacktrace.MeccaException backtrace = deserialized.backtraces.get(0);
      assertThat(backtrace.name).isEqualTo(Thread.currentThread().getName());
      assertThat(backtrace.faulted).isEqualTo(true);
      List<MeccaBacktrace.StackElement> stackElements = backtrace.backtrace;
      assertBacktracesMatch(myLittleStackTrace, stackElements);
      assertThat(deserialized.ivars).isEmpty();
      assertThat(deserialized.log_message).isEqualTo(logMessage);
      assertThat(deserialized.message).isEqualTo(message);
      final List<MeccaBacktrace.NestedException> nestedExceptions = deserialized.parent_exceptions;
      assertThat(nestedExceptions).hasSize(2);
  
      final MeccaBacktrace.NestedException nested1 = nestedExceptions.get(0);
      assertThat(nested1.class_name).isEqualTo(nestedException.getClass().getName());
      assertThat(nested1.ivars).isEmpty();
      assertThat(nested1.message).isEqualTo(nestedExceptionMessage);
      backtrace = nested1.backtraces.get(0);
      assertThat(backtrace.name).isEqualTo(Thread.currentThread().getName());
      assertThat(backtrace.faulted).isEqualTo(true);
      assertBacktracesMatch(nestedStackTrace, backtrace.backtrace);
  
      final MeccaBacktrace.NestedException nested2 = nestedExceptions.get(1);
      assertThat(nested2.class_name).isEqualTo(doublyNestedException.getClass().getName());
      assertThat(nested2.ivars).isEmpty();
      assertThat(nested2.message).isEqualTo(doublyNestedExceptionMessage);
      backtrace = nested1.backtraces.get(0);
      assertThat(backtrace.name).isEqualTo(Thread.currentThread().getName());
      assertThat(backtrace.faulted).isEqualTo(true);
      assertBacktracesMatch(nestedStackTrace, backtrace.backtrace);
    }
  
    private class EntryFactory {
      public MeccaEntry create(String logMessage, Throwable exception) {
        return new MeccaEntry("testclient", "testAPIKey", logMessage, exception, "testAppVersion",
            42, "testSHA", "testDeviceId", "testEndpoint", "testUserId", "Debug");
      }
    }
  }