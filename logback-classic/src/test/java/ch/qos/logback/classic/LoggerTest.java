/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.status.Status;

public class LoggerTest {

  LoggerContext lc = new LoggerContext();
  Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
  Logger loggerTest = lc.getLogger(LoggerTest.class);

  ListAppender<LoggingEvent> listAppender = new ListAppender<LoggingEvent>();

  @Test
  public void smoke() {
    ListAppender<LoggingEvent> listAppender = new ListAppender<LoggingEvent>();
    listAppender.start();
    root.addAppender(listAppender);
    Logger logger = lc.getLogger(LoggerTest.class);
    assertEquals(0, listAppender.list.size());
    logger.debug("hello");
    assertEquals(1, listAppender.list.size());
  }

  @Test
  public void testNoStart() {
    // listAppender.start();
    listAppender.setContext(lc);
    root.addAppender(listAppender);
    Logger logger = lc.getLogger(LoggerTest.class);
    logger.debug("hello");

    List<Status> statusList = lc.getStatusManager().getCopyOfStatusList();
    Status s0 = statusList.get(0);
    assertEquals(Status.WARN, s0.getLevel());
    assertTrue(s0.getMessage().startsWith("Attempted to append to non started"));
  }

  @Test
  public void testAdditive() {
    listAppender.start();
    root.addAppender(listAppender);
    loggerTest.addAppender(listAppender);
    loggerTest.setAdditive(false);
    loggerTest.debug("hello");
    // 1 instead of two, since logger is not additive
    assertEquals(1, listAppender.list.size());
  }

  @Test
  public void testRootLogger() {
    Logger logger = (Logger) LoggerFactory.getLogger(LoggerContext.ROOT_NAME);
    LoggerContext lc = logger.getLoggerContext();

    assertNotNull("Returned logger is null", logger);
    assertEquals("Return logger isn't named root", logger.getName(),
        LoggerContext.ROOT_NAME);
    assertTrue("logger instances should be indentical", logger == lc.root);
  }

  @Test
  public void testBasicFiltering() throws Exception {
    listAppender.start();
    root.addAppender(listAppender);
    root.setLevel(Level.INFO);
    loggerTest.debug("x");
    assertEquals(0, listAppender.list.size());
    loggerTest.info("x");
    loggerTest.warn("x");
    loggerTest.error("x");
    assertEquals(3, listAppender.list.size());
  }

  void checkLevelThreshold(Logger logger, Level threshold) {

    if (Level.ERROR_INT >= threshold.levelInt) {
      assertTrue(logger.isErrorEnabled());
      assertTrue(logger.isEnabledFor(Level.ERROR));
    } else {
      assertFalse(logger.isErrorEnabled());
      assertFalse(logger.isEnabledFor(Level.ERROR));
    }

    if (Level.WARN_INT >= threshold.levelInt) {
      assertTrue(logger.isWarnEnabled());
      assertTrue(logger.isEnabledFor(Level.WARN));
    } else {
      assertFalse(logger.isWarnEnabled());
      assertFalse(logger.isEnabledFor(Level.WARN));
    }
    if (Level.INFO_INT >= threshold.levelInt) {
      assertTrue(logger.isInfoEnabled());
      assertTrue(logger.isEnabledFor(Level.INFO));
    } else {
      assertFalse(logger.isInfoEnabled());
      assertFalse(logger.isEnabledFor(Level.INFO));
    }
    if (Level.DEBUG_INT >= threshold.levelInt) {
      assertTrue(logger.isDebugEnabled());
      assertTrue(logger.isEnabledFor(Level.DEBUG));
    } else {
      assertFalse(logger.isDebugEnabled());
      assertFalse(logger.isEnabledFor(Level.DEBUG));
    }
    if (Level.TRACE_INT >= threshold.levelInt) {
      assertTrue(logger.isTraceEnabled());
      assertTrue(logger.isEnabledFor(Level.TRACE));
    } else {
      assertFalse(logger.isTraceEnabled());
      assertFalse(logger.isEnabledFor(Level.TRACE));
    }

  }

  @Test
  public void testEnabled_All() throws Exception {
    root.setLevel(Level.ALL);
    checkLevelThreshold(loggerTest, Level.ALL);
  }

  @Test
  public void testEnabled_Debug() throws Exception {
    root.setLevel(Level.DEBUG);
    checkLevelThreshold(loggerTest, Level.DEBUG);
  }

  @Test
  public void testEnabled_Info() throws Exception {
    root.setLevel(Level.INFO);
    checkLevelThreshold(loggerTest, Level.INFO);
  }

  @Test
  public void testEnabledX_Warn() throws Exception {
    root.setLevel(Level.WARN);
    checkLevelThreshold(loggerTest, Level.WARN);
  }

  public void testEnabledX_Errror() throws Exception {
    root.setLevel(Level.ERROR);
    checkLevelThreshold(loggerTest, Level.ERROR);
  }

  @Test
  public void testEnabledX_Off() throws Exception {
    root.setLevel(Level.OFF);
    checkLevelThreshold(loggerTest, Level.OFF);
  }

  @Test
  public void setRootLevelToNull() {
    try {
      root.setLevel(null);
      fail("The level of the root logger should not be settable to null");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void setLevelToNull_A() {
    loggerTest.setLevel(null);
    assertEquals(root.getEffectiveLevel(), loggerTest.getEffectiveLevel());
  }
  
  @Test
  public void setLevelToNull_B() {
    loggerTest.setLevel(Level.DEBUG);
    loggerTest.setLevel(null);
    assertEquals(root.getEffectiveLevel(), loggerTest.getEffectiveLevel());
  }
  
  @Test
  public void setLevelToNull_LBCLASSIC_91() {
    loggerTest.setLevel(Level.DEBUG);
    lc.getLogger(loggerTest.getName() + ".child");
    loggerTest.setLevel(null);
    assertEquals(root.getEffectiveLevel(), loggerTest.getEffectiveLevel());
  }

}
