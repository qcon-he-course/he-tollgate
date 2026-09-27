/**
 * Domain events for the case lifecycle.
 *
 * <p>Events are immutable records published through Spring's {@code ApplicationEventPublisher} after the change they
 * describe has been made. They carry everything a listener needs, so listeners do not have to load the case again,
 * and they depend on nothing outside this package. Listeners run synchronously, in the publisher's transaction.
 *
 * <p>See ADR-0006. New case behaviour that reacts to something happening to a case should be a listener on one of
 * these events rather than another call added to the service that made the change.
 */
package com.halvern.tollgate.platform.events;
