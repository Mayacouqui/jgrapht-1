/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Lead:  Barak Naveh (barak_naveh@users.sourceforge.net)
 *
 * (C) Copyright 2003, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* ---------------------------
 * DefaultListenableGraph.java
 * ---------------------------
 * (C) Copyright 2003, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 04-Aug-2003 : Strong refs to listeners instead of weak refs (BN);
 * 10-Aug-2003 : Adaptation to new event model (BN);
 *
 */
package org._3pq.jgrapht.graph;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.Graph;
import org._3pq.jgrapht.ListenableGraph;
import org._3pq.jgrapht.WeightedGraph;
import org._3pq.jgrapht.event.EdgeEvent;
import org._3pq.jgrapht.event.GraphListener;
import org._3pq.jgrapht.event.VertexEvent;
import org._3pq.jgrapht.event.VertexSetListener;

/**
 * A graph backed by the the graph specified at the constructor, which can be
 * listened by <code>GraphListener</code>s and by
 * <code>VertexSetListener</code>s. Operations on this graph "pass through" to
 * the to the backing graph. Any modification made to this graph or the
 * backing graph is reflected by the other.
 * 
 * <p>
 * This graph does <i>not</i> pass the hashCode and equals operations through
 * to the backing graph, but relies on <tt>Object</tt>'s <tt>equals</tt> and
 * <tt>hashCode</tt> methods.
 * </p>
 *
 * @author Barak Naveh
 *
 * @see GraphListener
 * @see VertexSetListener
 * @since Jul 20, 2003
 */
public class DefaultListenableGraph extends GraphDelegator
    implements ListenableGraph, Cloneable {
    private ArrayList            m_graphListeners       = new ArrayList(  );
    private ArrayList            m_vertexSetListeners   = new ArrayList(  );
    private FlyweightEdgeEvent   m_reuseableEdgeEvent;
    private FlyweightVertexEvent m_reuseableVertexEvent;
    private boolean              m_reuseEvents;

    /**
     * Creates a new listenable graph.
     *
     * @param g the backing graph.
     */
    public DefaultListenableGraph( Graph g ) {
        this( g, false );
    }


    /**
     * Creates a new listenable graph. If the <code>reuseEvents</code> flag is
     * set to <code>true</code> this class will reuse previously fired events
     * and will not create a new object for each event. This option increases
     * performance but should be used with care, especially in multithreaded
     * environment.
     *
     * @param g the backing graph.
     * @param reuseEvents whether to reuse previously fired event objects
     *        instead of creating a new event object for each event.
     *
     * @throws IllegalArgumentException if the backing graph is already a
     *         listenable graph.
     */
    public DefaultListenableGraph( Graph g, boolean reuseEvents ) {
        super( g );
        m_reuseEvents              = reuseEvents;
        m_reuseableEdgeEvent       = new FlyweightEdgeEvent( this, -1, null );
        m_reuseableVertexEvent     = new FlyweightVertexEvent( this, -1, null );

        // the following restriction could be probably relaxed in the future.
        if( g instanceof ListenableGraph ) {
            throw new IllegalArgumentException( 
                "base graph cannot be listenable" );
        }
    }

    /**
     * If the <code>reuseEvents</code> flag is set to <code>true</code> this
     * class will reuse previously fired events and will not create a new
     * object for each event. This option increases performance but should be
     * used with care, especially in multithreaded environment.
     *
     * @param reuseEvents whether to reuse previously fired event objects
     *        instead of creating a new event object for each event.
     */
    public void setReuseEvents( boolean reuseEvents ) {
        m_reuseEvents = reuseEvents;
    }


    /**
     * Tests whether the <code>reuseEvents</code> flag is set. If the flag is
     * set to <code>true</code> this class will reuse previously fired events
     * and will not create a new object for each event. This option increases
     * performance but should be used with care, especially in multithreaded
     * environment.
     *
     * @return the value of the <code>reuseEvents</code> flag.
     */
    public boolean isReuseEvents(  ) {
        return m_reuseEvents;
    }


    /**
     * @see WeightedGraph#addEdge(Object, Object, double)
     */
    public Edge addEdge( Object sourceVertex, Object targetVertex, double weight ) {
        Edge e =
            ( (WeightedGraph) m_delegate ).addEdge( sourceVertex, targetVertex,
                weight );

        if( e != null ) {
            fireEdgeAdded( e );
        }

        return e;
    }


    /**
     * @see Graph#addEdge(Object, Object)
     */
    public Edge addEdge( Object sourceVertex, Object targetVertex ) {
        Edge e = m_delegate.addEdge( sourceVertex, targetVertex );

        if( e != null ) {
            fireEdgeAdded( e );
        }

        return e;
    }


    /**
     * @see Graph#addEdge(Edge)
     */
    public boolean addEdge( Edge e ) {
        boolean modified = m_delegate.addEdge( e );

        if( modified ) {
            fireEdgeAdded( e );
        }

        return modified;
    }


    /**
     * @see ListenableGraph#addGraphListener(GraphListener)
     */
    public void addGraphListener( GraphListener l ) {
        addToListenerList( m_graphListeners, l );
    }


    /**
     * @see Graph#addVertex(Object)
     */
    public boolean addVertex( Object v ) {
        boolean modified = m_delegate.addVertex( v );

        if( modified ) {
            fireVertexAdded( v );
        }

        return modified;
    }


    /**
     * @see ListenableGraph#addVertexSetListener(VertexSetListener)
     */
    public void addVertexSetListener( VertexSetListener l ) {
        addToListenerList( m_vertexSetListeners, l );
    }


    /**
     * @see java.lang.Object#clone()
     */
    public Object clone(  ) {
        try {
            DefaultListenableGraph g = (DefaultListenableGraph) super.clone(  );
            g.m_graphListeners         = (ArrayList) m_graphListeners.clone(  );
            g.m_vertexSetListeners     = (ArrayList) m_vertexSetListeners.clone(  );

            return g;
        }
         catch( CloneNotSupportedException e ) {
            // should never get here since we're Cloneable
            e.printStackTrace(  );
            throw new RuntimeException( "internal error" );
        }
    }


    /**
     * @see Graph#removeEdge(Object, Object)
     */
    public Edge removeEdge( Object sourceVertex, Object targetVertex ) {
        Edge e = m_delegate.removeEdge( sourceVertex, targetVertex );

        if( e != null ) {
            fireEdgeRemoved( e );
        }

        return e;
    }


    /**
     * @see Graph#removeEdge(Edge)
     */
    public boolean removeEdge( Edge e ) {
        boolean modified = m_delegate.removeEdge( e );

        if( modified ) {
            fireEdgeRemoved( e );
        }

        return modified;
    }


    /**
     * @see ListenableGraph#removeGraphListener(GraphListener)
     */
    public void removeGraphListener( GraphListener l ) {
        m_graphListeners.remove( l );
    }


    /**
     * @see Graph#removeVertex(Object)
     */
    public boolean removeVertex( Object v ) {
        boolean modified = m_delegate.removeVertex( v );

        if( modified ) {
            fireVertexRemoved( v );
        }

        return modified;
    }


    /**
     * @see ListenableGraph#removeVertexSetListener(VertexSetListener)
     */
    public void removeVertexSetListener( VertexSetListener l ) {
        m_vertexSetListeners.remove( l );
    }


    /**
     * Notify listeners that the specified edge was added.
     *
     * @param edge the edge that was added.
     */
    protected void fireEdgeAdded( Edge edge ) {
        EdgeEvent e   = createEdgeEvent( EdgeEvent.EDGE_ADDED, edge );
        int       len = m_graphListeners.size(  );

        for( int i = 0; i < len; i++ ) {
            GraphListener l = (GraphListener) m_graphListeners.get( i );

            l.edgeAdded( e );
        }
    }


    /**
     * Notify listeners that the specified edge was removed.
     *
     * @param edge the edge that was removed.
     */
    protected void fireEdgeRemoved( Edge edge ) {
        EdgeEvent e   = createEdgeEvent( EdgeEvent.EDGE_REMOVED, edge );
        int       len = m_graphListeners.size(  );

        for( int i = 0; i < len; i++ ) {
            GraphListener l = (GraphListener) m_graphListeners.get( i );

            l.edgeRemoved( e );
        }
    }


    /**
     * Notify listeners that the specified vertex was added.
     *
     * @param vertex the vertex that was added.
     */
    protected void fireVertexAdded( Object vertex ) {
        VertexEvent e   = createVertexEvent( VertexEvent.VERTEX_ADDED, vertex );
        int         len;

        len = m_vertexSetListeners.size(  );

        for( int i = 0; i < len; i++ ) {
            VertexSetListener l =
                (VertexSetListener) m_vertexSetListeners.get( i );

            l.vertexAdded( e );
        }

        len = m_graphListeners.size(  );

        for( int i = 0; i < len; i++ ) {
            GraphListener l = (GraphListener) m_graphListeners.get( i );

            l.vertexAdded( e );
        }
    }


    /**
     * Notify listeners that the specified vertex was removed.
     *
     * @param vertex the vertex that was removed.
     */
    protected void fireVertexRemoved( Object vertex ) {
        VertexEvent e = createVertexEvent( VertexEvent.VERTEX_REMOVED, vertex );
        int         len;

        len = m_vertexSetListeners.size(  );

        for( int i = 0; i < len; i++ ) {
            VertexSetListener l =
                (VertexSetListener) m_vertexSetListeners.get( i );

            l.vertexRemoved( e );
        }

        len = m_graphListeners.size(  );

        for( int i = 0; i < len; i++ ) {
            GraphListener l = (GraphListener) m_graphListeners.get( i );

            l.vertexRemoved( e );
        }
    }


    private void addToListenerList( List list, EventListener l ) {
        if( !list.contains( l ) ) {
            list.add( l );
        }
    }


    private EdgeEvent createEdgeEvent( int eventType, Edge edge ) {
        if( m_reuseEvents ) {
            m_reuseableEdgeEvent.setType( eventType );
            m_reuseableEdgeEvent.setEdge( edge );

            return m_reuseableEdgeEvent;
        }
        else {
            return new EdgeEvent( this, eventType, edge );
        }
    }


    private VertexEvent createVertexEvent( int eventType, Object vertex ) {
        if( m_reuseEvents ) {
            m_reuseableVertexEvent.setType( eventType );
            m_reuseableVertexEvent.setVertex( vertex );

            return m_reuseableVertexEvent;
        }
        else {
            return new VertexEvent( this, eventType, vertex );
        }
    }

    /**
     * A reuseable edge event.
     *
     * @author Barak Naveh
     *
     * @since Aug 10, 2003
     */
    private static class FlyweightEdgeEvent extends EdgeEvent {
        /**
         * @see EdgeEvent#EdgeEvent(Object, int, Edge)
         */
        public FlyweightEdgeEvent( Object eventSource, int type, Edge e ) {
            super( eventSource, type, e );
        }

        /**
         * Sets the edge of this event.
         *
         * @param e the edge to be set.
         */
        protected void setEdge( Edge e ) {
            m_edge = e;
        }


        /**
         * Set the event type of this event.
         *
         * @param type the type to be set.
         */
        protected void setType( int type ) {
            m_type = type;
        }
    }


    /**
     * A reuseable vertex event.
     *
     * @author Barak Naveh
     *
     * @since Aug 10, 2003
     */
    private static class FlyweightVertexEvent extends VertexEvent {
        /**
         * @see VertexEvent#VertexEvent(Object, int, Object)
         */
        public FlyweightVertexEvent( Object eventSource, int type, Object vertex ) {
            super( eventSource, type, vertex );
        }

        /**
         * Set the event type of this event.
         *
         * @param type type to be set.
         */
        protected void setType( int type ) {
            m_type = type;
        }


        /**
         * Sets the vertex of this event.
         *
         * @param vertex the vertex to be set.
         */
        protected void setVertex( Object vertex ) {
            m_vertex = vertex;
        }
    }
}
