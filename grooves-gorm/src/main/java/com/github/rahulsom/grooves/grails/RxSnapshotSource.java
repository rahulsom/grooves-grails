package com.github.rahulsom.grooves.grails;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import grails.gorm.rx.RxEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import rx.Observable;

import java.util.Date;

import static com.github.rahulsom.grooves.grails.QueryUtil.*;
import static org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod;
import static rx.RxReactiveStreams.toPublisher;

/**
 * Supplies Snapshots from a Blocking Gorm Source.
 *
 * @param <AggregateT>  The aggregate over which the query executes
 * @param <EventIdT>    The type of the Event's id field
 * @param <EventT>      The type of the Event
 * @param <SnapshotIdT> The type of the Snapshot's id field
 * @param <SnapshotT>   The type of the Snapshot
 *
 * @author Rahul Somasunderam
 */
public interface RxSnapshotSource<
        AggregateIdT,
        AggregateT extends GormAggregate<AggregateIdT>,
        EventIdT,
        EventT extends BaseEvent<AggregateT, EventIdT, EventT>,
        SnapshotIdT,
        SnapshotT extends Snapshot<AggregateT, SnapshotIdT, EventIdT, EventT> & RxEntity<SnapshotT>
        > extends QuerySupport<AggregateT, EventIdT, EventT, SnapshotIdT, SnapshotT> {

    Class<SnapshotT> getSnapshotClass();

    @NotNull
    @Override
    default Publisher<SnapshotT> getSnapshot(long maxPosition, @NotNull AggregateT aggregate) {
        //noinspection unchecked
        return toPublisher((Observable<SnapshotT>) (maxPosition == Long.MAX_VALUE ?
                invokeStaticMethod(getSnapshotClass(),
                        SNAPSHOTS_BY_AGGREGATE,
                        new Object[]{aggregate.getId(), LATEST_BY_POSITION}) :
                invokeStaticMethod(
                        getSnapshotClass(),
                        SNAPSHOTS_BY_POSITION,
                        new Object[]{aggregate.getId(), maxPosition, LATEST_BY_POSITION})));
    }

    @NotNull
    @Override
    default Publisher<SnapshotT> getSnapshot(
            @Nullable Date maxTimestamp, @NotNull AggregateT aggregate) {
        //noinspection unchecked
        return toPublisher((Observable<SnapshotT>) (maxTimestamp == null ?
                invokeStaticMethod(getSnapshotClass(),
                        SNAPSHOTS_BY_AGGREGATE,
                        new Object[]{aggregate.getId(), LATEST_BY_POSITION}) :
                invokeStaticMethod(getSnapshotClass(),
                        SNAPSHOTS_BY_TIMESTAMP,
                        new Object[]{aggregate.getId(), maxTimestamp, LATEST_BY_POSITION})));
    }

}
