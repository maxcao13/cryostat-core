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
package io.cryostat.core.serialization;

import java.util.Map;

import javax.management.ObjectName;

import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.QuantityConversionException;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.rjmx.services.jfr.IRecordingDescriptor;
import org.openjdk.jmc.rjmx.services.jfr.IRecordingDescriptor.RecordingState;

import jdk.jfr.Recording;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SerializableRecordingDescriptor {

    protected long id;
    protected String name;
    protected RecordingState state;
    protected long startTime;
    protected long duration;
    protected boolean continuous;
    protected boolean toDisk;
    protected long maxSize;
    protected long maxAge;

    public SerializableRecordingDescriptor(
            long id,
            String name,
            RecordingState state,
            long startTime,
            long duration,
            boolean continuous,
            boolean toDisk,
            long maxSize,
            long maxAge) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.startTime = startTime;
        this.duration = duration;
        this.continuous = continuous;
        this.toDisk = toDisk;
        this.maxSize = maxSize;
        this.maxAge = maxAge;
    }

    public SerializableRecordingDescriptor(Recording recording) {
        this(
                recording.getId(),
                recording.getName(),
                RecordingState.valueOf(recording.getState().name()),
                recording.getStartTime() == null ? 0 : recording.getStartTime().toEpochMilli(),
                recording.getDuration() == null ? 0 : recording.getDuration().toMillis(),
                recording.getDuration() == null,
                recording.isToDisk(),
                recording.getMaxSize(),
                recording.getMaxAge() == null ? 0 : recording.getMaxAge().toMillis());
    }

    public SerializableRecordingDescriptor(IRecordingDescriptor orig)
            throws QuantityConversionException {
        this.id = orig.getId();
        this.name = orig.getName();
        this.state = orig.getState();
        this.startTime = orig.getStartTime().longValueIn(UnitLookup.EPOCH_MS);
        this.duration = orig.getDuration().longValueIn(UnitLookup.MILLISECOND);
        this.continuous = orig.isContinuous();
        this.toDisk = orig.getToDisk();
        this.maxSize = orig.getMaxSize().longValueIn(UnitLookup.BYTE);
        this.maxAge = orig.getMaxAge().longValueIn(UnitLookup.MILLISECOND);
    }

    public SerializableRecordingDescriptor(SerializableRecordingDescriptor o) {
        this.id = o.getId();
        this.name = o.getName();
        this.state = o.getState();
        this.startTime = o.getStartTime();
        this.duration = o.getDuration();
        this.continuous = o.isContinuous();
        this.toDisk = o.getToDisk();
        this.maxSize = o.getMaxSize();
        this.maxAge = o.getMaxAge();
    }

    public IRecordingDescriptor toJmcForm() {
        return new IRecordingDescriptor() {

            @Override
            public Long getId() {
                return id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public RecordingState getState() {
                return state;
            }

            @Override
            public Map<String, ?> getOptions() {
                return Map.of();
            }

            @Override
            public ObjectName getObjectName() {
                return null;
            }

            @Override
            public IQuantity getDataStartTime() {
                return getStartTime();
            }

            @Override
            public IQuantity getDataEndTime() {
                return getStartTime().add(getDuration());
            }

            @Override
            public IQuantity getStartTime() {
                return UnitLookup.EPOCH_MS.quantity(startTime);
            }

            @Override
            public IQuantity getDuration() {
                return UnitLookup.MILLISECOND.quantity(duration);
            }

            @Override
            public boolean isContinuous() {
                return continuous;
            }

            @Override
            public boolean getToDisk() {
                return toDisk;
            }

            @Override
            public IQuantity getMaxSize() {
                return UnitLookup.BYTE.quantity(maxSize);
            }

            @Override
            public IQuantity getMaxAge() {
                return UnitLookup.MILLISECOND.quantity(maxAge);
            }
        };
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RecordingState getState() {
        return state;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isContinuous() {
        return continuous;
    }

    public boolean getToDisk() {
        return toDisk;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public long getMaxAge() {
        return maxAge;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }
}
