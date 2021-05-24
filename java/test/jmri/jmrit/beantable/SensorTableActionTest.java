package jmri.jmrit.beantable;

import jmri.util.gui.GuiLafPreferencesManager;
import java.awt.GraphicsEnvironment;

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.JTextField;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrix.internal.InternalSensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrit.beantable.SensorTableAction class.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SensorTableActionTest extends AbstractTableActionBase<Sensor> {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", a);
    }

    @Override
    public String getTableFrameName() {
        return Bundle.getMessage("TitleSensorTable");
    }

    @Override
    @Test
    public void testGetClassDescription() {
        Assert.assertEquals("Sensor Table Action class description", "Sensor Table", a.getClassDescription());
    }

    /**
     * Check the return value of includeAddButton. The table generated by this
     * action includes an Add Button.
     */
    @Override
    @Test
    public void testIncludeAddButton() {
        Assert.assertTrue("Default include add button", a.includeAddButton());
    }

    /**
     * Check graphic state presentation.
     *
     * @since 4.7.4
     */
    @Test
    public void testAddAndInvoke() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        a.actionPerformed(null); // show table
        // create 2 sensors and see if they exist
        Sensor is1 = InstanceManager.sensorManagerInstance().provideSensor("IS1");
        Sensor is2 = InstanceManager.sensorManagerInstance().provideSensor("IS2");
        try {
            is1.setKnownState(Sensor.ACTIVE);
            is2.setKnownState(Sensor.INACTIVE);
        } catch (jmri.JmriException reason) {
            log.warn("Exception flipping sensor is1: {}", reason);
        }

        // set graphic state column display preference to false, read by createModel()
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setGraphicTableState(false);

        SensorTableAction _sTable;
        _sTable = new SensorTableAction();
        Assert.assertNotNull("found SensorTable frame", _sTable);

        // set to true, use icons
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setGraphicTableState(true);
        SensorTableAction _s1Table;
        _s1Table = new SensorTableAction();
        Assert.assertNotNull("found SensorTable1 frame", _s1Table);

        _s1Table.addPressed(null);
        JFrame af = JFrameOperator.waitJFrame(Bundle.getMessage("TitleAddSensor"), true, true);
        Assert.assertNotNull("found Add frame", af);
        // close AddPane
        _s1Table.cancelPressed(null);
        // more Sensor Add pane tests in SensorTableWindowTest

        // clean up
        JFrame f = a.getFrame();
        if (f != null) {
            JUnitUtil.dispose(f);
        }
        JUnitUtil.dispose(af);
        _sTable.dispose();
        _s1Table.dispose();
    }

    @Override
    public String getAddFrameName(){
        return Bundle.getMessage("TitleAddSensor");
    }

    @Test
    @Override
    public void testAddButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeTrue(a.includeAddButton());
        a.actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(getTableFrameName(), true, true);

        // find the "Add... " button and press it.
        jmri.util.swing.JemmyUtil.pressButton(new JFrameOperator(f),Bundle.getMessage("ButtonAdd"));
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        JFrame f1 = JFrameOperator.waitJFrame(getAddFrameName(), true, true);
        jmri.util.swing.JemmyUtil.pressButton(new JFrameOperator(f1),Bundle.getMessage("ButtonClose")); // not sure why this is close in this frame.
        JUnitUtil.dispose(f1);
        JUnitUtil.dispose(f);
    }

    @Test
    @Override
    public void testEditButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeTrue(a.includeAddButton());
        a.actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(getTableFrameName(), true, true);

        // find the "Add... " button and press it.
        JFrameOperator jfo = new JFrameOperator(f);
        JemmyUtil.pressButton(jfo,Bundle.getMessage("ButtonAdd"));
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        JFrame f1 = JFrameOperator.waitJFrame(getAddFrameName(), true, true);
        //Enter 1 in the text field labeled "Hardware address:"
        JTextField hwAddressField = JTextFieldOperator.findJTextField(f1, new NameComponentChooser("hwAddressTextField"));
        Assert.assertNotNull("hwAddressTextField", hwAddressField);

        // set to "1"
        new JTextFieldOperator(hwAddressField).typeText("1");
        //and press create 
        JemmyUtil.pressButton(new JFrameOperator(f1),Bundle.getMessage("ButtonCreate"));
        new org.netbeans.jemmy.QueueTool().waitEmpty();

        JTableOperator tbl = new JTableOperator(jfo, 0);
        // find the "Edit" button and press it.  This is in the table body.
        tbl.clickOnCell(0,jmri.jmrit.beantable.sensor.SensorTableDataModel.EDITCOL);

        JFrame f2 = JFrameOperator.waitJFrame(getEditFrameName(), true, true);
        JemmyUtil.pressButton(new JFrameOperator(f2),Bundle.getMessage("ButtonCancel"));
        JUnitUtil.dispose(f2);
        JUnitUtil.dispose(f1);
        JUnitUtil.dispose(f);
    }

    @Override
    public String getEditFrameName(){
        return "Edit Sensor IS1";
    }
    
    @Test
    public void testAddFailureCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        InstanceManager.setDefault(SensorManager.class, new CreateNewSensorAlwaysException());
        
        a = new SensorTableAction();
        Assume.assumeTrue(a.includeAddButton());
        
        a.actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(getTableFrameName(), true, true);
        // find the "Add... " button and press it.
        JemmyUtil.pressButton(new JFrameOperator(f), Bundle.getMessage("ButtonAdd"));
        
        JFrame f1 = JFrameOperator.waitJFrame(getAddFrameName(), true, true);
        JTextField hwAddressField = JTextFieldOperator.findJTextField(f1, new NameComponentChooser("hwAddressTextField"));
        Assert.assertNotNull("hwAddressTextField", hwAddressField);
        // set to "1"
        new JTextFieldOperator(hwAddressField).setText("1");
        Thread add1 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("ErrorBeanCreateFailed","Sensor" , "IS1"), Bundle.getMessage("ButtonOK"));  // NOI18N
        
        //and press create
        JemmyUtil.pressButton(new JFrameOperator(f1), Bundle.getMessage("ButtonCreate"));
        JUnitUtil.waitFor(()->{return !(add1.isAlive());}, "dialog finished");  // NOI18N
        
        JemmyUtil.pressButton(new JFrameOperator(f1), Bundle.getMessage("ButtonClose")); // not sure why this is close in this frame.
        JUnitUtil.dispose(f1);
        JUnitUtil.dispose(f);
    }
    
    private class CreateNewSensorAlwaysException extends InternalSensorManager {

        public CreateNewSensorAlwaysException() {
            super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull
        protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
            throw new IllegalArgumentException("createNewSensor Exception Text");
        }
        
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSensorManager();
        helpTarget = "package.jmri.jmrit.beantable.SensorTable"; 
        a = new SensorTableAction();
    }

    @Override
    @AfterEach
    public void tearDown() {
        a.dispose();
        a = null;
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SensorTableActionTest.class);

}
