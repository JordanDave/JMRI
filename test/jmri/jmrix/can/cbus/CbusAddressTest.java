// CbusAddressTest.java

package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanMessage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.cbus.SensorAddress class.
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version     $Revision: 1.7 $
 */
public class CbusAddressTest extends TestCase {

    public void testCanCreate() {
        assertTrue(null != new CbusAddress("X0A;+N15E6"));
    }

    public void testCbusAddressOK() {
        // +/- form
        assertTrue(new CbusAddress("+001").check());
        assertTrue(new CbusAddress("-001").check());
        
        // hex form
        assertTrue(new CbusAddress("x0ABC").check());
        assertTrue(new CbusAddress("x0abc").check());
        assertTrue(new CbusAddress("xa1b2c3").check());
        assertTrue(new CbusAddress("x123456789ABCDEF0").check());
        
        // n0e0 form
        assertTrue(new CbusAddress("+n1e2").check());
        assertTrue(new CbusAddress("+n01e002").check());
        assertTrue(new CbusAddress("+1e2").check());
        assertTrue(new CbusAddress("-n1e2").check());
        assertTrue(new CbusAddress("-n01e002").check());
        assertTrue(new CbusAddress("-1e2").check());
        assertTrue(new CbusAddress("n1e2").check());
        assertTrue(new CbusAddress("n01e002").check());
        assertTrue(new CbusAddress("1e2").check());
        assertTrue(new CbusAddress("+n12e34").check());
        assertTrue(new CbusAddress("+n12e35").check());        
    }

    public void testCbusAddressNotOK() {
        assertTrue(!new CbusAddress("+0A1").check());
        assertTrue(!new CbusAddress("- 001").check());
        assertTrue(!new CbusAddress("ABC").check());

        assertTrue(!new CbusAddress("xABC").check());    // odd number of digits     
        assertTrue(!new CbusAddress("xprs0").check());

        assertTrue(!new CbusAddress("+n1e").check());
        assertTrue(!new CbusAddress("+ne1").check());
        assertTrue(!new CbusAddress("+e1").check());
        assertTrue(!new CbusAddress("+n1").check());

        // multiple address not OK
        assertTrue(!new CbusAddress("+1;+1;+1").check());
    }

    public void testCbusIdParseMatchReply() {
        assertTrue(new CbusAddress("+12").match(
                new CanReply(
                    new int[]{CbusConstants.CBUS_ACON,0x00,0x00,0x00,12}
        )));
        assertTrue(new CbusAddress("-12").match(
                new CanReply(
                    new int[]{CbusConstants.CBUS_ACOF,0x00,0x00,0x00,12}
        )));
        assertTrue(new CbusAddress("x123456789ABCDEF0").match(
                new CanReply(
                    new int[]{0x12,0x34,0x56,0x78,
                              0x9A,0xBC,0xDE,0xF0}
        )));
    }
    
    public void testCbusIdParseMatchMessage() {
        assertTrue(new CbusAddress("+12").match(
                new CanMessage(
                    new int[]{CbusConstants.CBUS_ACON,0x00,0x00,0x00,12}
        )));
        assertTrue(new CbusAddress("-12").match(
                new CanMessage(
                    new int[]{CbusConstants.CBUS_ACOF,0x00,0x00,0x00,12}
        )));
        assertTrue(new CbusAddress("x123456789ABCDEF0").match(
                new CanMessage(
                    new int[]{0x12,0x34,0x56,0x78,
                              0x9A,0xBC,0xDE,0xF0}
        )));
    }
    
    public void testNEformMatch() {
        assertTrue(new CbusAddress("+n12e34").match(
                new CanMessage(
                    new int[]{CbusConstants.CBUS_ACON,0x00,12,0x00,34}
        )));

        assertTrue(new CbusAddress("+12e34").match(
                new CanMessage(
                    new int[]{CbusConstants.CBUS_ACON,0x00,12,0x00,34}
        )));

        assertTrue(new CbusAddress("12e34").match(
                new CanMessage(
                    new int[]{CbusConstants.CBUS_ACON,0x00,12,0x00,34}
        )));

        assertTrue(new CbusAddress("n12e34").match(
                new CanMessage(
                    new int[]{CbusConstants.CBUS_ACON,0x00,12,0x00,34}
        )));

        assertTrue(new CbusAddress("-n12e34").match(
                new CanMessage(
                    new int[]{CbusConstants.CBUS_ACOF,0x00,12,0x00,34}
        )));
    }
    
    public void testCbusIdNotParse() {
        assertTrue(!new CbusAddress("-12").match(
                new CanReply(
                    new int[]{CbusConstants.CBUS_ACON,0x00,0x00,0x00,12}
        )));
        assertTrue(!new CbusAddress("-268").match(
                new CanReply(
                    new int[]{CbusConstants.CBUS_ACOF,0x00,0x00,0x00,12}
        )));
    }
    
    public void testPlusMinus() {
        assertTrue( (new CbusAddress("+001")).equals(new CbusAddress("+001")));        
        assertTrue( (new CbusAddress("+001")).equals(new CbusAddress("x9000000001")));
        assertTrue( (new CbusAddress("-200003")).equals(new CbusAddress("x9100020003")));
        
    }

    public void testEqualsOK() {
        assertTrue( (new CbusAddress("+001")).equals(new CbusAddress("+001")));
        assertTrue( (new CbusAddress("+001")).equals(new CbusAddress("x9000000001")));
    }
    
    public void testSplitCheckOK() {
        assertTrue(new CbusAddress("+001").checkSplit());
        assertTrue(new CbusAddress("-001").checkSplit());
        assertTrue(new CbusAddress("x0ABC").checkSplit());        
        assertTrue(new CbusAddress("x0abc").checkSplit());        
        assertTrue(new CbusAddress("xa1b2c3").checkSplit());        
        assertTrue(new CbusAddress("x123456789ABCDEF0").checkSplit());        

        assertTrue(new CbusAddress("+001;+001").checkSplit());
        assertTrue(new CbusAddress("-001;+001").checkSplit());
        assertTrue(new CbusAddress("x0ABC;+001").checkSplit());        
        assertTrue(new CbusAddress("x0abc;+001").checkSplit());        
        assertTrue(new CbusAddress("xa1b2c3;+001").checkSplit());        
        assertTrue(new CbusAddress("x123456789ABCDEF0;+001").checkSplit());        
    }
    
    public void testMultiTermSplitCheckOK() {
        assertTrue(new CbusAddress("+1;+1;+1").checkSplit());
        assertTrue(new CbusAddress("+1;+1").checkSplit());
        assertTrue(new CbusAddress("+n12e34;+1").checkSplit());
        assertTrue(new CbusAddress("+1;x1234").checkSplit());
        assertTrue(new CbusAddress("+1;n12e34").checkSplit());
        assertTrue(new CbusAddress("+1;+n12e34").checkSplit());
        assertTrue(new CbusAddress("+n12e34;+n12e35").checkSplit());
        assertTrue(new CbusAddress("x0A;+n15E6").checkSplit());
    }
        
    public void testSplitCheckNotOK() {
        assertTrue(!new CbusAddress("+0A1").check());
        assertTrue(!new CbusAddress("- 001").check());
        assertTrue(!new CbusAddress("ABC").check());        
        assertTrue(!new CbusAddress("xprs0").check());        

        assertTrue(!new CbusAddress("+1;;+1").checkSplit());
        assertTrue(!new CbusAddress("+001;").checkSplit());
        assertTrue(!new CbusAddress("-001;").checkSplit());
        assertTrue(!new CbusAddress("-001;;").checkSplit());
        assertTrue(!new CbusAddress("xABC;").checkSplit());
        assertTrue(!new CbusAddress("xabc;").checkSplit());
        assertTrue(!new CbusAddress("xa1b2c3;").checkSplit());
        assertTrue(!new CbusAddress("x123456789ABCDEF0;").checkSplit());   

        assertTrue(!new CbusAddress("+001;xprs0").checkSplit());
        assertTrue(!new CbusAddress("-001;xprs0").checkSplit());
        assertTrue(!new CbusAddress("xABC;xprs0").checkSplit());
        assertTrue(!new CbusAddress("xabc;xprs0").checkSplit());
        assertTrue(!new CbusAddress("xa1b2c3;xprs0").checkSplit());
        assertTrue(!new CbusAddress("x123456789ABCDEF0;xprs0").checkSplit());
    }

    public void testSplit() {
        CbusAddress a;
        CbusAddress[] v;
        
        a = new CbusAddress("+001");
        v = a.split();
        assertTrue(v.length==1);
        assertTrue(new CbusAddress("+001").equals(v[0]));
        
        a = new CbusAddress("+001;-2");
        v = a.split();
        assertTrue(v.length==2);
        assertTrue(new CbusAddress("+001").equals(v[0]));
        assertTrue(new CbusAddress("-2").equals(v[1]));
        
    }

    // from here down is testing infrastructure

    public CbusAddressTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
    	String[] testCaseName = {CbusAddressTest.class.getName()};
    	junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CbusAddressTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CbusAddressTest.class.getName());
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
