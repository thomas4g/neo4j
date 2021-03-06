/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j Enterprise Edition. The included source
 * code can be redistributed and/or modified under the terms of the
 * GNU AFFERO GENERAL PUBLIC LICENSE Version 3
 * (http://www.fsf.org/licensing/licenses/agpl-3.0.html) with the
 * Commons Clause, as found in the associated LICENSE.txt file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * Neo4j object code can be licensed independently from the source
 * under separate terms from the AGPL. Inquiries can be directed to:
 * licensing@neo4j.com
 *
 * More information is also available at:
 * https://neo4j.com/licensing/
 */
package org.neo4j.cluster.protocol.atomicbroadcast.multipaxos.context;

import java.net.URI;

import org.neo4j.cluster.InstanceId;
import org.neo4j.cluster.protocol.cluster.ClusterConfiguration;
import org.neo4j.logging.Log;

class CommonContextState
{
    private URI boundAt;
    private long lastKnownLearnedInstanceInCluster = -1;
    private org.neo4j.cluster.InstanceId lastKnownAliveUpToDateInstance;
    private long nextInstanceId;
    private ClusterConfiguration configuration;
    private final int maxAcceptors;

    CommonContextState( ClusterConfiguration configuration, int maxAcceptors )
    {
        this.configuration = configuration;
        this.maxAcceptors = maxAcceptors;
    }

    private CommonContextState( URI boundAt, long lastKnownLearnedInstanceInCluster, long nextInstanceId,
                        ClusterConfiguration configuration, int maxAcceptors )
    {
        this.boundAt = boundAt;
        this.lastKnownLearnedInstanceInCluster = lastKnownLearnedInstanceInCluster;
        this.nextInstanceId = nextInstanceId;
        this.configuration = configuration;
        this.maxAcceptors = maxAcceptors;
    }

    public URI boundAt()
    {
        return boundAt;
    }

    public void setBoundAt( InstanceId me, URI boundAt )
    {
        this.boundAt = boundAt;
        configuration.getMembers().put( me, boundAt );
    }

    public long lastKnownLearnedInstanceInCluster()
    {
        return lastKnownLearnedInstanceInCluster;
    }

    public void setLastKnownLearnedInstanceInCluster( long lastKnownLearnedInstanceInCluster, InstanceId instanceId )
    {
        if ( this.lastKnownLearnedInstanceInCluster <= lastKnownLearnedInstanceInCluster )
        {
            this.lastKnownLearnedInstanceInCluster = lastKnownLearnedInstanceInCluster;
            if ( instanceId != null )
            {
                this.lastKnownAliveUpToDateInstance = instanceId;
            }
        }
        else if ( lastKnownLearnedInstanceInCluster == -1 )
        {
            // Special case for clearing the state
            this.lastKnownLearnedInstanceInCluster = -1;
        }
    }

    public org.neo4j.cluster.InstanceId getLastKnownAliveUpToDateInstance()
    {
        return this.lastKnownAliveUpToDateInstance;
    }

    public long nextInstanceId()
    {
        return nextInstanceId;
    }

    public void setNextInstanceId( long nextInstanceId )
    {
        this.nextInstanceId = nextInstanceId;
    }

    public long getAndIncrementInstanceId()
    {
        return nextInstanceId++;
    }

    public ClusterConfiguration configuration()
    {
        return configuration;
    }

    public void setConfiguration( ClusterConfiguration configuration )
    {
        this.configuration = configuration;
    }

    public int getMaxAcceptors()
    {
        return maxAcceptors;
    }

    public CommonContextState snapshot( Log log )
    {
        return new CommonContextState( boundAt, lastKnownLearnedInstanceInCluster, nextInstanceId,
                configuration.snapshot(log), maxAcceptors );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        CommonContextState that = (CommonContextState) o;

        if ( lastKnownLearnedInstanceInCluster != that.lastKnownLearnedInstanceInCluster )
        {
            return false;
        }
        if ( nextInstanceId != that.nextInstanceId )
        {
            return false;
        }
        if ( boundAt != null ? !boundAt.equals( that.boundAt ) : that.boundAt != null )
        {
            return false;
        }
        return configuration != null ? configuration.equals( that.configuration ) : that.configuration == null;
    }

    @Override
    public int hashCode()
    {
        int result = boundAt != null ? boundAt.hashCode() : 0;
        result = 31 * result + (int) (lastKnownLearnedInstanceInCluster ^ (lastKnownLearnedInstanceInCluster >>> 32));
        result = 31 * result + (int) (nextInstanceId ^ (nextInstanceId >>> 32));
        result = 31 * result + (configuration != null ? configuration.hashCode() : 0);
        return result;
    }
}
