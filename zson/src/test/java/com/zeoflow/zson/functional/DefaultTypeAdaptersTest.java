/*
 * Copyright (C) 2020 ZeoFlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zeoflow.zson.functional;

import com.zeoflow.zson.Zson;
import com.zeoflow.zson.ZsonBuilder;
import com.zeoflow.zson.JsonArray;
import com.zeoflow.zson.JsonDeserializationContext;
import com.zeoflow.zson.JsonDeserializer;
import com.zeoflow.zson.JsonElement;
import com.zeoflow.zson.JsonNull;
import com.zeoflow.zson.JsonObject;
import com.zeoflow.zson.JsonParseException;
import com.zeoflow.zson.JsonPrimitive;
import com.zeoflow.zson.JsonSyntaxException;
import com.zeoflow.zson.TypeAdapter;
import com.zeoflow.zson.internal.JavaVersion;
import com.zeoflow.zson.reflect.TypeToken;
import com.zeoflow.zson.stream.JsonReader;
import com.zeoflow.zson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;

import junit.framework.TestCase;

/**
 * Functional test for Json serialization and deserialization for common classes for which default
 * support is provided in Zson. The tests for Map types are available in {@link MapTest}.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class DefaultTypeAdaptersTest extends TestCase {
  private Zson zson;
  private TimeZone oldTimeZone;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.oldTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    Locale.setDefault(Locale.US);
    zson = new Zson();
  }

  @Override
  protected void tearDown() throws Exception {
    TimeZone.setDefault(oldTimeZone);
    super.tearDown();
  }

  public void testClassSerialization() {
    try {
      zson.toJson(String.class);
    } catch (UnsupportedOperationException expected) {}
    // Override with a custom type adapter for class.
    zson = new ZsonBuilder().registerTypeAdapter(Class.class, new MyClassTypeAdapter()).create();
    assertEquals("\"java.lang.String\"", zson.toJson(String.class));
  }

  public void testClassDeserialization() {
    try {
      zson.fromJson("String.class", String.class.getClass());
    } catch (UnsupportedOperationException expected) {}
    // Override with a custom type adapter for class.
    zson = new ZsonBuilder().registerTypeAdapter(Class.class, new MyClassTypeAdapter()).create();
    assertEquals(String.class, zson.fromJson("java.lang.String", Class.class));
  }

  public void testUrlSerialization() throws Exception {
    String urlValue = "http://google.com/";
    URL url = new URL(urlValue);
    assertEquals("\"http://google.com/\"", zson.toJson(url));
  }

  public void testUrlDeserialization() {
    String urlValue = "http://google.com/";
    String json = "'http:\\/\\/google.com\\/'";
    URL target = zson.fromJson(json, URL.class);
    assertEquals(urlValue, target.toExternalForm());

    zson.fromJson('"' + urlValue + '"', URL.class);
    assertEquals(urlValue, target.toExternalForm());
  }

  public void testUrlNullSerialization() throws Exception {
    ClassWithUrlField target = new ClassWithUrlField();
    assertEquals("{}", zson.toJson(target));
  }

  public void testUrlNullDeserialization() {
    String json = "{}";
    ClassWithUrlField target = zson.fromJson(json, ClassWithUrlField.class);
    assertNull(target.url);
  }

  private static class ClassWithUrlField {
    URL url;
  }

  public void testUriSerialization() throws Exception {
    String uriValue = "http://google.com/";
    URI uri = new URI(uriValue);
    assertEquals("\"http://google.com/\"", zson.toJson(uri));
  }

  public void testUriDeserialization() {
    String uriValue = "http://google.com/";
    String json = '"' + uriValue + '"';
    URI target = zson.fromJson(json, URI.class);
    assertEquals(uriValue, target.toASCIIString());
  }
  
  public void testNullSerialization() throws Exception {
    testNullSerializationAndDeserialization(Boolean.class);
    testNullSerializationAndDeserialization(Byte.class);
    testNullSerializationAndDeserialization(Short.class);
    testNullSerializationAndDeserialization(Integer.class);
    testNullSerializationAndDeserialization(Long.class);
    testNullSerializationAndDeserialization(Double.class);
    testNullSerializationAndDeserialization(Float.class);
    testNullSerializationAndDeserialization(Number.class);
    testNullSerializationAndDeserialization(Character.class);
    testNullSerializationAndDeserialization(String.class);
    testNullSerializationAndDeserialization(StringBuilder.class);
    testNullSerializationAndDeserialization(StringBuffer.class);
    testNullSerializationAndDeserialization(BigDecimal.class);
    testNullSerializationAndDeserialization(BigInteger.class);
    testNullSerializationAndDeserialization(TreeSet.class);
    testNullSerializationAndDeserialization(ArrayList.class);
    testNullSerializationAndDeserialization(HashSet.class);
    testNullSerializationAndDeserialization(Properties.class);
    testNullSerializationAndDeserialization(URL.class);
    testNullSerializationAndDeserialization(URI.class);
    testNullSerializationAndDeserialization(UUID.class);
    testNullSerializationAndDeserialization(Locale.class);
    testNullSerializationAndDeserialization(InetAddress.class);
    testNullSerializationAndDeserialization(BitSet.class);
    testNullSerializationAndDeserialization(Date.class);
    testNullSerializationAndDeserialization(GregorianCalendar.class);
    testNullSerializationAndDeserialization(Calendar.class);
    testNullSerializationAndDeserialization(Time.class);
    testNullSerializationAndDeserialization(Timestamp.class);
    testNullSerializationAndDeserialization(java.sql.Date.class);
    testNullSerializationAndDeserialization(Enum.class);
    testNullSerializationAndDeserialization(Class.class);
  }

  private void testNullSerializationAndDeserialization(Class<?> c) {
    assertEquals("null", zson.toJson(null, c));
    assertEquals(null, zson.fromJson("null", c));
  }

  public void testUuidSerialization() throws Exception {
    String uuidValue = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    UUID uuid = UUID.fromString(uuidValue);
    assertEquals('"' + uuidValue + '"', zson.toJson(uuid));
  }

  public void testUuidDeserialization() {
    String uuidValue = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    String json = '"' + uuidValue + '"';
    UUID target = zson.fromJson(json, UUID.class);
    assertEquals(uuidValue, target.toString());
  }

  public void testLocaleSerializationWithLanguage() {
    Locale target = new Locale("en");
    assertEquals("\"en\"", zson.toJson(target));
  }

  public void testLocaleDeserializationWithLanguage() {
    String json = "\"en\"";
    Locale locale = zson.fromJson(json, Locale.class);
    assertEquals("en", locale.getLanguage());
  }

  public void testLocaleSerializationWithLanguageCountry() {
    Locale target = Locale.CANADA_FRENCH;
    assertEquals("\"fr_CA\"", zson.toJson(target));
  }

  public void testLocaleDeserializationWithLanguageCountry() {
    String json = "\"fr_CA\"";
    Locale locale = zson.fromJson(json, Locale.class);
    assertEquals(Locale.CANADA_FRENCH, locale);
  }

  public void testLocaleSerializationWithLanguageCountryVariant() {
    Locale target = new Locale("de", "DE", "EURO");
    String json = zson.toJson(target);
    assertEquals("\"de_DE_EURO\"", json);
  }

  public void testLocaleDeserializationWithLanguageCountryVariant() {
    String json = "\"de_DE_EURO\"";
    Locale locale = zson.fromJson(json, Locale.class);
    assertEquals("de", locale.getLanguage());
    assertEquals("DE", locale.getCountry());
    assertEquals("EURO", locale.getVariant());
  }

  public void testBigDecimalFieldSerialization() {
    ClassWithBigDecimal target = new ClassWithBigDecimal("-122.01e-21");
    String json = zson.toJson(target);
    String actual = json.substring(json.indexOf(':') + 1, json.indexOf('}'));
    assertEquals(target.value, new BigDecimal(actual));
  }

  public void testBigDecimalFieldDeserialization() {
    ClassWithBigDecimal expected = new ClassWithBigDecimal("-122.01e-21");
    String json = expected.getExpectedJson();
    ClassWithBigDecimal actual = zson.fromJson(json, ClassWithBigDecimal.class);
    assertEquals(expected.value, actual.value);
  }

  public void testBadValueForBigDecimalDeserialization() {
    try {
      zson.fromJson("{\"value\"=1.5e-1.0031}", ClassWithBigDecimal.class);
      fail("Exponent of a BigDecimal must be an integer value.");
    } catch (JsonParseException expected) { }
  }

  public void testBigIntegerFieldSerialization() {
    ClassWithBigInteger target = new ClassWithBigInteger("23232323215323234234324324324324324324");
    String json = zson.toJson(target);
    assertEquals(target.getExpectedJson(), json);
  }

  public void testBigIntegerFieldDeserialization() {
    ClassWithBigInteger expected = new ClassWithBigInteger("879697697697697697697697697697697697");
    String json = expected.getExpectedJson();
    ClassWithBigInteger actual = zson.fromJson(json, ClassWithBigInteger.class);
    assertEquals(expected.value, actual.value);
  }
  
  public void testOverrideBigIntegerTypeAdapter() throws Exception {
    zson = new ZsonBuilder()
        .registerTypeAdapter(BigInteger.class, new NumberAsStringAdapter(BigInteger.class))
        .create();
    assertEquals("\"123\"", zson.toJson(new BigInteger("123"), BigInteger.class));
    assertEquals(new BigInteger("123"), zson.fromJson("\"123\"", BigInteger.class));
  }

  public void testOverrideBigDecimalTypeAdapter() throws Exception {
    zson = new ZsonBuilder()
        .registerTypeAdapter(BigDecimal.class, new NumberAsStringAdapter(BigDecimal.class))
        .create();
    assertEquals("\"1.1\"", zson.toJson(new BigDecimal("1.1"), BigDecimal.class));
    assertEquals(new BigDecimal("1.1"), zson.fromJson("\"1.1\"", BigDecimal.class));
  }

  public void testSetSerialization() throws Exception {
    Zson zson = new Zson();
    HashSet<String> s = new HashSet<String>();
    s.add("blah");
    String json = zson.toJson(s);
    assertEquals("[\"blah\"]", json);

    json = zson.toJson(s, Set.class);
    assertEquals("[\"blah\"]", json);
  }

  public void testBitSetSerialization() throws Exception {
    Zson zson = new Zson();
    BitSet bits = new BitSet();
    bits.set(1);
    bits.set(3, 6);
    bits.set(9);
    String json = zson.toJson(bits);
    assertEquals("[0,1,0,1,1,1,0,0,0,1]", json);
  }

  public void testBitSetDeserialization() throws Exception {
    BitSet expected = new BitSet();
    expected.set(0);
    expected.set(2, 6);
    expected.set(8);

    Zson zson = new Zson();
    String json = zson.toJson(expected);
    assertEquals(expected, zson.fromJson(json, BitSet.class));

    json = "[1,0,1,1,1,1,0,0,1,0,0,0]";
    assertEquals(expected, zson.fromJson(json, BitSet.class));

    json = "[\"1\",\"0\",\"1\",\"1\",\"1\",\"1\",\"0\",\"0\",\"1\"]";
    assertEquals(expected, zson.fromJson(json, BitSet.class));

    json = "[true,false,true,true,true,true,false,false,true,false,false]";
    assertEquals(expected, zson.fromJson(json, BitSet.class));
  }

  public void testDefaultDateSerialization() {
    Date now = new Date(1315806903103L);
    String json = zson.toJson(now);
    if (JavaVersion.isJava9OrLater()) {
      assertEquals("\"Sep 11, 2011, 10:55:03 PM\"", json);
    } else {
      assertEquals("\"Sep 11, 2011 10:55:03 PM\"", json);
    }
  }

  public void testDefaultDateDeserialization() {
    String json = "'Dec 13, 2009 07:18:02 AM'";
    Date extracted = zson.fromJson(json, Date.class);
    assertEqualsDate(extracted, 2009, 11, 13);
    assertEqualsTime(extracted, 7, 18, 2);
  }

  // Date can not directly be compared with another instance since the deserialization loses the
  // millisecond portion.
  @SuppressWarnings("deprecation")
  private void assertEqualsDate(Date date, int year, int month, int day) {
    assertEquals(year-1900, date.getYear());
    assertEquals(month, date.getMonth());
    assertEquals(day, date.getDate());
  }

  @SuppressWarnings("deprecation")
  private void assertEqualsTime(Date date, int hours, int minutes, int seconds) {
    assertEquals(hours, date.getHours());
    assertEquals(minutes, date.getMinutes());
    assertEquals(seconds, date.getSeconds());
  }

  public void testDefaultJavaSqlDateSerialization() {
    java.sql.Date instant = new java.sql.Date(1259875082000L);
    String json = zson.toJson(instant);
    assertEquals("\"Dec 3, 2009\"", json);
  }

  public void testDefaultJavaSqlDateDeserialization() {
    String json = "'Dec 3, 2009'";
    java.sql.Date extracted = zson.fromJson(json, java.sql.Date.class);
    assertEqualsDate(extracted, 2009, 11, 3);
  }

  public void testDefaultJavaSqlTimestampSerialization() {
    Timestamp now = new java.sql.Timestamp(1259875082000L);
    String json = zson.toJson(now);
    if (JavaVersion.isJava9OrLater()) {
      assertEquals("\"Dec 3, 2009, 1:18:02 PM\"", json);
    } else {
      assertEquals("\"Dec 3, 2009 1:18:02 PM\"", json);
    }
  }

  public void testDefaultJavaSqlTimestampDeserialization() {
    String json = "'Dec 3, 2009 1:18:02 PM'";
    Timestamp extracted = zson.fromJson(json, Timestamp.class);
    assertEqualsDate(extracted, 2009, 11, 3);
    assertEqualsTime(extracted, 13, 18, 2);
  }

  public void testDefaultJavaSqlTimeSerialization() {
    Time now = new Time(1259875082000L);
    String json = zson.toJson(now);
    assertEquals("\"01:18:02 PM\"", json);
  }

  public void testDefaultJavaSqlTimeDeserialization() {
    String json = "'1:18:02 PM'";
    Time extracted = zson.fromJson(json, Time.class);
    assertEqualsTime(extracted, 13, 18, 2);
  }

  public void testDefaultDateSerializationUsingBuilder() throws Exception {
    Zson zson = new ZsonBuilder().create();
    Date now = new Date(1315806903103L);
    String json = zson.toJson(now);
    if (JavaVersion.isJava9OrLater()) {
      assertEquals("\"Sep 11, 2011, 10:55:03 PM\"", json);
    } else {
      assertEquals("\"Sep 11, 2011 10:55:03 PM\"", json);
    }
  }

  public void testDefaultDateDeserializationUsingBuilder() throws Exception {
    Zson zson = new ZsonBuilder().create();
    Date now = new Date(1315806903103L);
    String json = zson.toJson(now);
    Date extracted = zson.fromJson(json, Date.class);
    assertEquals(now.toString(), extracted.toString());
  }

  public void testDefaultCalendarSerialization() throws Exception {
    Zson zson = new ZsonBuilder().create();
    String json = zson.toJson(Calendar.getInstance());
    assertTrue(json.contains("year"));
    assertTrue(json.contains("month"));
    assertTrue(json.contains("dayOfMonth"));
    assertTrue(json.contains("hourOfDay"));
    assertTrue(json.contains("minute"));
    assertTrue(json.contains("second"));
  }

  public void testDefaultCalendarDeserialization() throws Exception {
    Zson zson = new ZsonBuilder().create();
    String json = "{year:2009,month:2,dayOfMonth:11,hourOfDay:14,minute:29,second:23}";
    Calendar cal = zson.fromJson(json, Calendar.class);
    assertEquals(2009, cal.get(Calendar.YEAR));
    assertEquals(2, cal.get(Calendar.MONTH));
    assertEquals(11, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(29, cal.get(Calendar.MINUTE));
    assertEquals(23, cal.get(Calendar.SECOND));
  }

  public void testDefaultGregorianCalendarSerialization() throws Exception {
    Zson zson = new ZsonBuilder().create();
    GregorianCalendar cal = new GregorianCalendar();
    String json = zson.toJson(cal);
    assertTrue(json.contains("year"));
    assertTrue(json.contains("month"));
    assertTrue(json.contains("dayOfMonth"));
    assertTrue(json.contains("hourOfDay"));
    assertTrue(json.contains("minute"));
    assertTrue(json.contains("second"));
  }

  public void testDefaultGregorianCalendarDeserialization() throws Exception {
    Zson zson = new ZsonBuilder().create();
    String json = "{year:2009,month:2,dayOfMonth:11,hourOfDay:14,minute:29,second:23}";
    GregorianCalendar cal = zson.fromJson(json, GregorianCalendar.class);
    assertEquals(2009, cal.get(Calendar.YEAR));
    assertEquals(2, cal.get(Calendar.MONTH));
    assertEquals(11, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(29, cal.get(Calendar.MINUTE));
    assertEquals(23, cal.get(Calendar.SECOND));
  }

  public void testDateSerializationWithPattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    Zson zson = new ZsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    Date now = new Date(1315806903103L);
    String json = zson.toJson(now);
    assertEquals("\"2011-09-11\"", json);
  }

  @SuppressWarnings("deprecation")
  public void testDateDeserializationWithPattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    Zson zson = new ZsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    Date now = new Date(1315806903103L);
    String json = zson.toJson(now);
    Date extracted = zson.fromJson(json, Date.class);
    assertEquals(now.getYear(), extracted.getYear());
    assertEquals(now.getMonth(), extracted.getMonth());
    assertEquals(now.getDay(), extracted.getDay());
  }

  public void testDateSerializationWithPatternNotOverridenByTypeAdapter() throws Exception {
    String pattern = "yyyy-MM-dd";
    Zson zson = new ZsonBuilder()
        .setDateFormat(pattern)
        .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
          public Date deserialize(JsonElement json, Type typeOfT,
              JsonDeserializationContext context)
              throws JsonParseException {
            return new Date(1315806903103L);
          }
        })
        .create();

    Date now = new Date(1315806903103L);
    String json = zson.toJson(now);
    assertEquals("\"2011-09-11\"", json);
  }

  // http://code.google.com/p/google-Zson/issues/detail?id=230
  public void testDateSerializationInCollection() throws Exception {
    Type listOfDates = new TypeToken<List<Date>>() {}.getType();
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      Zson zson = new ZsonBuilder().setDateFormat("yyyy-MM-dd").create();
      List<Date> dates = Arrays.asList(new Date(0));
      String json = zson.toJson(dates, listOfDates);
      assertEquals("[\"1970-01-01\"]", json);
      assertEquals(0L, zson.<List<Date>>fromJson("[\"1970-01-01\"]", listOfDates).get(0).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  // http://code.google.com/p/google-Zson/issues/detail?id=230
  public void testTimestampSerialization() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      Timestamp timestamp = new Timestamp(0L);
      Zson zson = new ZsonBuilder().setDateFormat("yyyy-MM-dd").create();
      String json = zson.toJson(timestamp, Timestamp.class);
      assertEquals("\"1970-01-01\"", json);
      assertEquals(0, zson.fromJson("\"1970-01-01\"", Timestamp.class).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  // http://code.google.com/p/google-Zson/issues/detail?id=230
  public void testSqlDateSerialization() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      java.sql.Date sqlDate = new java.sql.Date(0L);
      Zson zson = new ZsonBuilder().setDateFormat("yyyy-MM-dd").create();
      String json = zson.toJson(sqlDate, Timestamp.class);
      assertEquals("\"1970-01-01\"", json);
      assertEquals(0, zson.fromJson("\"1970-01-01\"", java.sql.Date.class).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  public void testJsonPrimitiveSerialization() {
    assertEquals("5", zson.toJson(new JsonPrimitive(5), JsonElement.class));
    assertEquals("true", zson.toJson(new JsonPrimitive(true), JsonElement.class));
    assertEquals("\"foo\"", zson.toJson(new JsonPrimitive("foo"), JsonElement.class));
    assertEquals("\"a\"", zson.toJson(new JsonPrimitive('a'), JsonElement.class));
  }

  public void testJsonPrimitiveDeserialization() {
    assertEquals(new JsonPrimitive(5), zson.fromJson("5", JsonElement.class));
    assertEquals(new JsonPrimitive(5), zson.fromJson("5", JsonPrimitive.class));
    assertEquals(new JsonPrimitive(true), zson.fromJson("true", JsonElement.class));
    assertEquals(new JsonPrimitive(true), zson.fromJson("true", JsonPrimitive.class));
    assertEquals(new JsonPrimitive("foo"), zson.fromJson("\"foo\"", JsonElement.class));
    assertEquals(new JsonPrimitive("foo"), zson.fromJson("\"foo\"", JsonPrimitive.class));
    assertEquals(new JsonPrimitive('a'), zson.fromJson("\"a\"", JsonElement.class));
    assertEquals(new JsonPrimitive('a'), zson.fromJson("\"a\"", JsonPrimitive.class));
  }

  public void testJsonNullSerialization() {
    assertEquals("null", zson.toJson(JsonNull.INSTANCE, JsonElement.class));
    assertEquals("null", zson.toJson(JsonNull.INSTANCE, JsonNull.class));
  }

  public void testNullJsonElementSerialization() {
    assertEquals("null", zson.toJson(null, JsonElement.class));
    assertEquals("null", zson.toJson(null, JsonNull.class));
  }

  public void testJsonArraySerialization() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(1));
    array.add(new JsonPrimitive(2));
    array.add(new JsonPrimitive(3));
    assertEquals("[1,2,3]", zson.toJson(array, JsonElement.class));
  }

  public void testJsonArrayDeserialization() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(1));
    array.add(new JsonPrimitive(2));
    array.add(new JsonPrimitive(3));

    String json = "[1,2,3]";
    assertEquals(array, zson.fromJson(json, JsonElement.class));
    assertEquals(array, zson.fromJson(json, JsonArray.class));
  }

  public void testJsonObjectSerialization() {
    JsonObject object = new JsonObject();
    object.add("foo", new JsonPrimitive(1));
    object.add("bar", new JsonPrimitive(2));
    assertEquals("{\"foo\":1,\"bar\":2}", zson.toJson(object, JsonElement.class));
  }

  public void testJsonObjectDeserialization() {
    JsonObject object = new JsonObject();
    object.add("foo", new JsonPrimitive(1));
    object.add("bar", new JsonPrimitive(2));

    String json = "{\"foo\":1,\"bar\":2}";
    JsonElement actual = zson.fromJson(json, JsonElement.class);
    assertEquals(object, actual);

    JsonObject actualObj = zson.fromJson(json, JsonObject.class);
    assertEquals(object, actualObj);
  }

  public void testJsonNullDeserialization() {
    assertEquals(JsonNull.INSTANCE, zson.fromJson("null", JsonElement.class));
    assertEquals(JsonNull.INSTANCE, zson.fromJson("null", JsonNull.class));
  }

  public void testJsonElementTypeMismatch() {
    try {
      zson.fromJson("\"abc\"", JsonObject.class);
      fail();
    } catch (JsonSyntaxException expected) {
      assertEquals("Expected a com.google.Zson.JsonObject but was com.google.Zson.JsonPrimitive",
          expected.getMessage());
    }
  }

  private static class ClassWithBigDecimal {
    BigDecimal value;
    ClassWithBigDecimal(String value) {
      this.value = new BigDecimal(value);
    }
    String getExpectedJson() {
      return "{\"value\":" + value.toEngineeringString() + "}";
    }
  }

  private static class ClassWithBigInteger {
    BigInteger value;
    ClassWithBigInteger(String value) {
      this.value = new BigInteger(value);
    }
    String getExpectedJson() {
      return "{\"value\":" + value + "}";
    }
  }

  public void testPropertiesSerialization() {
    Properties props = new Properties();
    props.setProperty("foo", "bar");
    String json = zson.toJson(props);
    String expected = "{\"foo\":\"bar\"}";
    assertEquals(expected, json);
  }

  public void testPropertiesDeserialization() {
    String json = "{foo:'bar'}";
    Properties props = zson.fromJson(json, Properties.class);
    assertEquals("bar", props.getProperty("foo"));
  }

  public void testTreeSetSerialization() {
    TreeSet<String> treeSet = new TreeSet<String>();
    treeSet.add("Value1");
    String json = zson.toJson(treeSet);
    assertEquals("[\"Value1\"]", json);
  }

  public void testTreeSetDeserialization() {
    String json = "['Value1']";
    Type type = new TypeToken<TreeSet<String>>() {}.getType();
    TreeSet<String> treeSet = zson.fromJson(json, type);
    assertTrue(treeSet.contains("Value1"));
  }

  public void testStringBuilderSerialization() {
    StringBuilder sb = new StringBuilder("abc");
    String json = zson.toJson(sb);
    assertEquals("\"abc\"", json);
  }

  public void testStringBuilderDeserialization() {
    StringBuilder sb = zson.fromJson("'abc'", StringBuilder.class);
    assertEquals("abc", sb.toString());
  }

  public void testStringBufferSerialization() {
    StringBuffer sb = new StringBuffer("abc");
    String json = zson.toJson(sb);
    assertEquals("\"abc\"", json);
  }

  public void testStringBufferDeserialization() {
    StringBuffer sb = zson.fromJson("'abc'", StringBuffer.class);
    assertEquals("abc", sb.toString());
  }

  @SuppressWarnings("rawtypes")
  private static class MyClassTypeAdapter extends TypeAdapter<Class> {
    @Override
    public void write(JsonWriter out, Class value) throws IOException {
      out.value(value.getName());
    }
    @Override
    public Class read(JsonReader in) throws IOException {
      String className = in.nextString();
      try {
        return Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new IOException(e);
      }
    }
  }

  static class NumberAsStringAdapter extends TypeAdapter<Number> {
    private final Constructor<? extends Number> constructor;
    NumberAsStringAdapter(Class<? extends Number> type) throws Exception {
      this.constructor = type.getConstructor(String.class);
    }
    @Override public void write(JsonWriter out, Number value) throws IOException {
      out.value(value.toString());
    }
    @Override public Number read(JsonReader in) throws IOException {
      try {
        return constructor.newInstance(in.nextString());
      } catch (Exception e) {
        throw new AssertionError(e);
      }
    }
  }
}