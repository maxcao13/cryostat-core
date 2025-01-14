/*
 * Copyright The Cryostat Authors
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or data
 * (collectively the "Software"), free of charge and under any and all copyright
 * rights in the Software, and any and all patent rights owned or freely
 * licensable by each licensor hereunder covering either (i) the unmodified
 * Software as contributed to or provided by such licensor, or (ii) the Larger
 * Works (as defined below), to deal in both
 *
 * (a) the Software, and
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software (each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 * The above copyright notice and either this complete permission notice or at
 * a minimum a reference to the UPL must be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.cryostat.core.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openjdk.jmc.common.unit.IConstrainedMap;
import org.openjdk.jmc.common.unit.IDescribedMap;
import org.openjdk.jmc.common.unit.IOptionDescriptor;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.QuantityConversionException;
import org.openjdk.jmc.flightrecorder.configuration.events.EventOptionID;
import org.openjdk.jmc.flightrecorder.configuration.events.IEventTypeID;
import org.openjdk.jmc.rjmx.ConnectionException;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.rjmx.ServiceNotAvailableException;
import org.openjdk.jmc.rjmx.services.jfr.FlightRecorderException;
import org.openjdk.jmc.rjmx.services.jfr.IEventTypeInfo;
import org.openjdk.jmc.rjmx.services.jfr.IFlightRecorderService;
import org.openjdk.jmc.rjmx.services.jfr.IRecordingDescriptor;
import org.openjdk.jmc.rjmx.services.jfr.internal.FlightRecorderServiceFactory;
import org.openjdk.jmc.rjmx.services.jfr.internal.FlightRecorderServiceV2;

import io.cryostat.core.EventOptionsBuilder;
import io.cryostat.core.EventOptionsBuilder.EventOptionException;
import io.cryostat.core.EventOptionsBuilder.EventTypeException;
import io.cryostat.core.templates.Template;
import io.cryostat.core.templates.TemplateType;
import io.cryostat.core.tui.ClientWriter;

public class JmxFlightRecorderService implements CryostatFlightRecorderService {

    private final JFRJMXConnection conn;
    private final IFlightRecorderService delegate;
    private final ClientWriter cw;

    JmxFlightRecorderService(JFRJMXConnection conn, ClientWriter cw)
            throws ConnectionException, ServiceNotAvailableException, IOException {
        this.conn = conn;
        if (!conn.isConnected()) {
            conn.connect();
        }
        IFlightRecorderService service =
                new FlightRecorderServiceFactory().getServiceInstance(conn.getHandle());
        if (service == null || !conn.isConnected()) {
            throw new ConnectionException(
                    String.format(
                            "Could not connect to remote target %s",
                            conn.connectionDescriptor.createJMXServiceURL().toString()));
        }
        this.delegate = service;
        this.cw = cw;
    }

    @Override
    public List<IRecordingDescriptor> getAvailableRecordings() throws FlightRecorderException {
        return delegate.getAvailableRecordings();
    }

    @Override
    public IRecordingDescriptor getSnapshotRecording() throws FlightRecorderException {
        return delegate.getSnapshotRecording();
    }

    @Override
    public IRecordingDescriptor getUpdatedRecordingDescription(IRecordingDescriptor descriptor)
            throws FlightRecorderException {
        return delegate.getUpdatedRecordingDescription(descriptor);
    }

    @Override
    public IRecordingDescriptor start(
            IConstrainedMap<String> recordingOptions, IConstrainedMap<EventOptionID> eventOptions)
            throws FlightRecorderException {
        return delegate.start(recordingOptions, eventOptions);
    }

    @Override
    public void stop(IRecordingDescriptor descriptor) throws FlightRecorderException {
        delegate.stop(descriptor);
    }

    @Override
    public void close(IRecordingDescriptor descriptor) throws FlightRecorderException {
        delegate.close(descriptor);
    }

    @Override
    public Map<String, IOptionDescriptor<?>> getAvailableRecordingOptions()
            throws FlightRecorderException {
        return delegate.getAvailableRecordingOptions();
    }

    @Override
    public IConstrainedMap<String> getRecordingOptions(IRecordingDescriptor recording)
            throws FlightRecorderException {
        return delegate.getRecordingOptions(recording);
    }

    @Override
    public Collection<? extends IEventTypeInfo> getAvailableEventTypes()
            throws FlightRecorderException {
        return delegate.getAvailableEventTypes();
    }

    @Override
    public Map<? extends IEventTypeID, ? extends IEventTypeInfo> getEventTypeInfoMapByID()
            throws FlightRecorderException {
        return delegate.getEventTypeInfoMapByID();
    }

    @Override
    public IConstrainedMap<EventOptionID> getCurrentEventTypeSettings()
            throws FlightRecorderException {
        return delegate.getCurrentEventTypeSettings();
    }

    @Override
    public IConstrainedMap<EventOptionID> getEventSettings(IRecordingDescriptor recording)
            throws FlightRecorderException {
        return delegate.getEventSettings(recording);
    }

    @Override
    public InputStream openStream(IRecordingDescriptor descriptor, boolean removeOnClose)
            throws FlightRecorderException {
        return delegate.openStream(descriptor, removeOnClose);
    }

    @Override
    public InputStream openStream(
            IRecordingDescriptor descriptor,
            IQuantity startTime,
            IQuantity endTime,
            boolean removeOnClose)
            throws FlightRecorderException {
        return delegate.openStream(descriptor, startTime, endTime, removeOnClose);
    }

    @Override
    public InputStream openStream(
            IRecordingDescriptor descriptor, IQuantity lastPartDuration, boolean removeOnClose)
            throws FlightRecorderException {
        return delegate.openStream(descriptor, lastPartDuration, removeOnClose);
    }

    @Override
    public List<String> getServerTemplates() throws FlightRecorderException {
        return delegate.getServerTemplates();
    }

    @Override
    public void updateEventOptions(
            IRecordingDescriptor descriptor, IConstrainedMap<EventOptionID> options)
            throws FlightRecorderException {
        delegate.updateEventOptions(descriptor, options);
    }

    @Override
    public void updateRecordingOptions(
            IRecordingDescriptor descriptor, IConstrainedMap<String> options)
            throws FlightRecorderException {
        delegate.updateRecordingOptions(descriptor, options);
    }

    @Override
    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    @Override
    public void enable() throws FlightRecorderException {
        delegate.enable();
    }

    @Override
    public String getVersion() {
        return delegate.getVersion();
    }

    @Override
    public IDescribedMap<String> getDefaultRecordingOptions() {
        return delegate.getDefaultRecordingOptions();
    }

    @Override
    public IDescribedMap<EventOptionID> getDefaultEventOptions() {
        return delegate.getDefaultEventOptions();
    }

    @Override
    public IRecordingDescriptor start(
            IConstrainedMap<String> recordingOptions,
            String templateName,
            TemplateType preferredTemplateType)
            throws io.cryostat.core.FlightRecorderException, FlightRecorderException,
                    ConnectionException, IOException, FlightRecorderException,
                    ServiceNotAvailableException, QuantityConversionException, EventOptionException,
                    EventTypeException {
        return delegate.start(recordingOptions, enableEvents(templateName, preferredTemplateType));
    }

    private IConstrainedMap<EventOptionID> enableEvents(
            String templateName, TemplateType templateType)
            throws ConnectionException, IOException, io.cryostat.core.FlightRecorderException,
                    FlightRecorderException, ServiceNotAvailableException,
                    QuantityConversionException, EventOptionException, EventTypeException {
        if (templateName.equals("ALL")) {
            return enableAllEvents();
        }
        // if template type not specified, try to find a Custom template by that name. If none,
        // fall back on finding a Target built-in template by the name. If not, throw an
        // exception and bail out.
        TemplateType type = getPreferredTemplateType(conn, templateName, templateType);
        return conn.getTemplateService().getEvents(templateName, type).get();
    }

    private IConstrainedMap<EventOptionID> enableAllEvents()
            throws ConnectionException, IOException, FlightRecorderException,
                    ServiceNotAvailableException, QuantityConversionException, EventOptionException,
                    EventTypeException {

        IConnectionHandle handle = conn.getHandle();
        EventOptionsBuilder builder =
                new EventOptionsBuilder(
                        cw, conn, () -> FlightRecorderServiceV2.isAvailable(handle));

        for (IEventTypeInfo eventTypeInfo : conn.getService().getAvailableEventTypes()) {
            builder.addEvent(eventTypeInfo.getEventTypeID().getFullKey(), "enabled", "true");
        }

        return builder.build();
    }

    private TemplateType getPreferredTemplateType(
            JFRConnection connection, String templateName, TemplateType templateType)
            throws io.cryostat.core.FlightRecorderException {
        if (templateType != null) {
            return templateType;
        }
        List<Template> matchingNameTemplates =
                connection.getTemplateService().getTemplates().stream()
                        .filter(t -> t.getName().equals(templateName))
                        .collect(Collectors.toList());
        boolean custom =
                matchingNameTemplates.stream()
                        .anyMatch(t -> t.getType().equals(TemplateType.CUSTOM));
        if (custom) {
            return TemplateType.CUSTOM;
        }
        boolean target =
                matchingNameTemplates.stream()
                        .anyMatch(t -> t.getType().equals(TemplateType.TARGET));
        if (target) {
            return TemplateType.TARGET;
        }
        throw new IllegalArgumentException(
                String.format("Invalid/unknown event template %s", templateName));
    }
}
