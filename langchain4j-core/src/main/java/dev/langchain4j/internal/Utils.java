package dev.langchain4j.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

public class Utils {
  private Utils() {}

  /**
   * Returns the given value if it is not {@code null}, otherwise returns the given default value.
   * @param value The value to return if it is not {@code null}.
   * @param defaultValue The value to return if the value is {@code null}.
   * @return the given value if it is not {@code null}, otherwise returns the given default value.
   * @param <T> The type of the value.
   */
  public static <T> T getOrDefault(T value, T defaultValue) {
    return value != null ? value : defaultValue;
  }

  /**
   * Returns the given value if it is not {@code null}, otherwise returns the value returned by the given supplier.
   * @param value The value to return if it is not {@code null}.
   * @param defaultValueSupplier The supplier to call if the value is {@code null}.
   * @return the given value if it is not {@code null}, otherwise returns the value returned by the given supplier.
   * @param <T> The type of the value.
   */
  public static <T> T getOrDefault(T value, Supplier<T> defaultValueSupplier) {
    return value != null ? value : defaultValueSupplier.get();
  }

  /**
   * Is the given string {@code null} or blank?
   * @param string The string to check.
   * @return true if the string is {@code null} or blank.
   */
  public static boolean isNullOrBlank(String string) {
    return string == null || string.trim().isEmpty();
  }

  /**
   * Is the given string not {@code null} and not blank?
   * @param string The string to check.
   * @return true if there's something in the string.
   */
  public static boolean isNotNullOrBlank(String string) {
    return !isNullOrBlank(string);
  }

  /**
   * Are all the given strings not {@code null} and not blank?
   * @param strings The strings to check.
   * @return {@code true} if every string is non-{@code null} and non-empty.
   */
  public static boolean areNotNullOrBlank(String... strings) {
    if (strings == null || strings.length == 0) {
      return false;
    }

    for (String string : strings) {
      if (isNullOrBlank(string)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Is the collection {@code null} or empty?
   * @param collection The collection to check.
   * @return {@code true} if the collection is {@code null} or {@link Collection#isEmpty()}, otherwise {@code false}.
   */
  public static boolean isNullOrEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  /**
   * @deprecated Use {@link #isNullOrEmpty(Collection)} instead.
   * @param collection The collection to check.
   * @return {@code true} if the collection is {@code null} or empty, {@code false} otherwise.
   */
  @SuppressWarnings("DeprecatedIsStillUsed")
  @Deprecated
  public static boolean isCollectionEmpty(Collection<?> collection) {
    return isNullOrEmpty(collection);
  }

  /**
   * Returns a string consisting of the given string repeated {@code times} times.
   *
   * @param string The string to repeat.
   * @param times  The number of times to repeat the string.
   * @return A string consisting of the given string repeated {@code times} times.
   */
  public static String repeat(String string, int times) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < times; i++) {
      sb.append(string);
    }
    return sb.toString();
  }

  /**
   * Returns a random UUID.
   * @return a UUID.
   */
  public static String randomUUID() {
    return UUID.randomUUID().toString();
  }

  /**
   * Internal method to get a SHA-256 instance of {@link MessageDigest}.
   * @return a {@link MessageDigest}.
   */
  @JacocoIgnoreCoverageGenerated
  private static MessageDigest getSha256Instance() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Generates a UUID from a hash of the given input string.
   * @param input The input string.
   * @return A UUID.
   */
  public static String generateUUIDFrom(String input) {
      byte[] hashBytes = getSha256Instance().digest(input.getBytes(UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : hashBytes) sb.append(String.format("%02x", b));
      return UUID.nameUUIDFromBytes(sb.toString().getBytes(UTF_8)).toString();
  }

  /**
   * Returns the given string surrounded by quotes.
   *
   * <p>If the given string is {@code null}, the string {@code "null"} is returned.
   *
   * @param string The string to quote.
   * @return The given string surrounded by quotes.
   */
  public static String quoted(String string) {
    if (string == null) {
      return "null";
    }
    return "\"" + string + "\"";
  }

  /**
   * Returns the first {@code numberOfChars} characters of the given string.
   * If the string is shorter than {@code numberOfChars}, the whole string is returned.
   *
   * @param string        The string to get the first characters from.
   * @param numberOfChars The number of characters to return.
   * @return The first {@code numberOfChars} characters of the given string.
   */
  public static String firstChars(String string, int numberOfChars) {
    if (string == null) {
      return null;
    }
    return string.length() > numberOfChars ? string.substring(0, numberOfChars) : string;
  }
}
