package edu.jhu.privtext.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import edu.jhu.privtext.util.encoders.test.EncodingTest;
import edu.jhu.privtext.util.encoders.test.GSM0338Test;

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
	EncodingTest.class, 
	GSM0338Test.class, 
	CipherWrapTest.class,
	SendTextTest.class
	})
public class AllTests { }