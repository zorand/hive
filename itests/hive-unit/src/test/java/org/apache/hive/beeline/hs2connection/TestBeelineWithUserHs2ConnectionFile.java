/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hive.beeline.hs2connection;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import org.apache.hadoop.hive.conf.HiveConf.ConfVars;
import org.apache.hive.jdbc.miniHS2.MiniHS2;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestBeelineWithUserHs2ConnectionFile extends BeelineWithHS2ConnectionFileTestBase {

  @Parameterized.Parameters(name = "{index}: tranportMode={0}")
  public static Collection<Object[]> transportModes() {
    return Arrays.asList(new Object[][] {
        { MiniHS2.HS2_ALL_MODE },{ MiniHS2.HS2_BINARY_MODE},{ MiniHS2.HS2_HTTP_MODE}
    });
  }

  @Test
  public void testBeelineConnectionHttp() throws Exception {
    Assume.assumeTrue(transportMode.equals(MiniHS2.HS2_HTTP_MODE)
        || transportMode.equalsIgnoreCase(MiniHS2.HS2_ALL_MODE));
    setupHttpHs2();
    String path = createHttpHs2ConnectionFile();
    testBeeLineConnection(path, new String[] { "-e", "show tables;" }, tableName);
  }

  private void setupHttpHs2() throws Exception {
    confOverlay.put(ConfVars.HIVE_SERVER2_ENABLE_DOAS.varname, "true");
    miniHS2.start(confOverlay);
    createTable();
  }

  private String createHttpHs2ConnectionFile() throws Exception {
    Hs2ConnectionXmlConfigFileWriter writer = new Hs2ConnectionXmlConfigFileWriter();
    String baseJdbcURL = miniHS2.getBaseHttpJdbcURL();

    URI uri = new URI(baseJdbcURL.substring(5));
    writer.writeProperty(HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "hosts",
        uri.getHost() + ":" + uri.getPort());
    writer.writeProperty(HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "user",
        System.getProperty("user.name"));
    writer.writeProperty(HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "password",
        "foo");
    writer.writeProperty(
        HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "transportMode",
        HS2_HTTP_MODE);
    writer.writeProperty(HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "httpPath",
        HS2_HTTP_ENDPOINT);
    writer.close();
    return writer.path();
  }

  @Test
  public void testBeelineConnectionNoAuth() throws Exception {
    Assume.assumeTrue(transportMode.equals(MiniHS2.HS2_BINARY_MODE)
        || transportMode.equalsIgnoreCase(MiniHS2.HS2_ALL_MODE));
    setupNoAuthConfHS2();
    String path = createNoAuthHs2ConnectionFile();
    testBeeLineConnection(path, new String[] { "-e", "show tables;" }, tableName);
  }

  private void setupNoAuthConfHS2() throws Exception {
    // use default configuration for no-auth mode
    miniHS2.start(confOverlay);
    createTable();
  }

  private String createNoAuthHs2ConnectionFile() throws Exception {
    Hs2ConnectionXmlConfigFileWriter writer = new Hs2ConnectionXmlConfigFileWriter();
    String baseJdbcURL = miniHS2.getBaseJdbcURL();
    URI uri = new URI(baseJdbcURL.substring(5));
    writer.writeProperty(HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "hosts",
        uri.getHost() + ":" + uri.getPort());
    writer.writeProperty(HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "user",
        System.getProperty("user.name"));
    writer.writeProperty(HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "password",
        "foo");
    writer.close();
    return writer.path();
  }

  @Test
  public void testBeelineConnectionSSL() throws Exception {
    Assume.assumeTrue(transportMode.equals(MiniHS2.HS2_BINARY_MODE)
        || transportMode.equalsIgnoreCase(MiniHS2.HS2_ALL_MODE));
    setupSslHs2();
    String path = createSSLHs2ConnectionFile();
    testBeeLineConnection(path, new String[] { "-e", "show tables;" }, tableName);
  }

  private String createSSLHs2ConnectionFile() throws Exception {
    Hs2ConnectionXmlConfigFileWriter writer = new Hs2ConnectionXmlConfigFileWriter();
    String baseJdbcURL = miniHS2.getBaseJdbcURL();
    URI uri = new URI(baseJdbcURL.substring(5));
    writer.writeProperty(HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "hosts",
        uri.getHost() + ":" + uri.getPort());
    writer.writeProperty(HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "user",
        System.getProperty("user.name"));
    writer.writeProperty(HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "password",
        "foo");
    writer.writeProperty(HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "ssl",
        "true");
    writer.writeProperty(
        HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "trustStorePassword",
        KEY_STORE_TRUST_STORE_PASSWORD);
    writer.writeProperty(
        HS2ConnectionFileParser.BEELINE_CONNECTION_PROPERTY_PREFIX + "sslTrustStore",
        dataFileDir + File.separator + TRUST_STORE_NAME);
    writer.close();
    return writer.path();
  }

  private void setupSslHs2() throws Exception {
    confOverlay.put(ConfVars.HIVE_SERVER2_USE_SSL.varname, "true");
    confOverlay.put(ConfVars.HIVE_SERVER2_SSL_KEYSTORE_PATH.varname,
        dataFileDir + File.separator + LOCALHOST_KEY_STORE_NAME);
    confOverlay.put(ConfVars.HIVE_SERVER2_SSL_KEYSTORE_PASSWORD.varname,
        KEY_STORE_TRUST_STORE_PASSWORD);
    miniHS2.start(confOverlay);
    createTable();
  }
}
