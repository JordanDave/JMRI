package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.*;
import jmri.util.ThreadingUtil;

/**
 * This action sets the state of a sensor.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionSensor extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanHandle<Sensor> _sensorHandle;
    private SensorState _sensorState = SensorState.Active;
    
    public ActionSensor(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = systemNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionSensor copy = new ActionSensor(sysName, userName);
        copy.setSensor(_sensorHandle);
        copy.setSensorState(_sensorState);
        return manager.registerAction(copy);
    }
    
    public void setSensor(@Nonnull String sensorName) {
        assertListenersAreNotRegistered(log, "setSensor");
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).getSensor(sensorName);
        if (sensor != null) {
            setSensor(sensor);
        } else {
            removeSensor();
            log.error("sensor \"{}\" is not found", sensorName);
        }
    }
    
    public void setSensor(@Nonnull NamedBeanHandle<Sensor> handle) {
        assertListenersAreNotRegistered(log, "setSensor");
        _sensorHandle = handle;
        InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
    }
    
    public void setSensor(@Nonnull Sensor sensor) {
        assertListenersAreNotRegistered(log, "setSensor");
        setSensor(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(sensor.getDisplayName(), sensor));
    }
    
    public void removeSensor() {
        assertListenersAreNotRegistered(log, "setSensor");
        if (_sensorHandle != null) {
            InstanceManager.sensorManagerInstance().removeVetoableChangeListener(this);
            _sensorHandle = null;
        }
    }
    
    public NamedBeanHandle<Sensor> getSensor() {
        return _sensorHandle;
    }
    
    public void setSensorState(SensorState state) {
        _sensorState = state;
    }
    
    public SensorState getSensorState() {
        return _sensorState;
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Sensor) {
                if (evt.getOldValue().equals(getSensor().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Sensor_SensorInUseSensorExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Sensor) {
                if (evt.getOldValue().equals(getSensor().getBean())) {
                    removeSensor();
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        if (_sensorHandle == null) return;
        
        final Sensor t = _sensorHandle.getBean();
        ThreadingUtil.runOnLayout(() -> {
            if (_sensorState == SensorState.Toggle) {
                if (t.getCommandedState() == Sensor.INACTIVE) {
                    t.setCommandedState(Sensor.ACTIVE);
                } else {
                    t.setCommandedState(Sensor.INACTIVE);
                }
            } else {
                t.setCommandedState(_sensorState.getID());
            }
        });
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Sensor_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String sensorName;
        if (_sensorHandle != null) {
            sensorName = _sensorHandle.getBean().getDisplayName();
        } else {
            sensorName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        return Bundle.getMessage(locale, "Sensor_Long", sensorName, _sensorState._text);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    // This constant is only used internally in SensorState but must be outside
    // the enum.
    private static final int TOGGLE_ID = -1;
    
    
    public enum SensorState {
        Inactive(Sensor.INACTIVE, Bundle.getMessage("SensorStateInactive")),
        Active(Sensor.ACTIVE, Bundle.getMessage("SensorStateActive")),
        Toggle(TOGGLE_ID, Bundle.getMessage("SensorToggleStatus"));
        
        private final int _id;
        private final String _text;
        
        private SensorState(int id, String text) {
            this._id = id;
            this._text = text;
        }
        
        static public SensorState get(int id) {
            switch (id) {
                case Sensor.INACTIVE:
                    return Inactive;
                    
                case Sensor.ACTIVE:
                    return Active;
                    
                case TOGGLE_ID:
                    return Toggle;
                    
                default:
                    throw new IllegalArgumentException("invalid sensor state");
            }
        }
        
        public int getID() {
            return _id;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSensor.class);
    
}
