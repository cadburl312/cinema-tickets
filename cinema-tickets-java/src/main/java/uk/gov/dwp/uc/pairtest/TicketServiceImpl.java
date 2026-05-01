package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.*;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private static final int MAX_TICKETS = 25;
    private static final int ADULT_PRICE = 25;
    private static final int CHILD_PRICE = 15;
    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException();
        }

        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException();
        }

        int sumAdults = 0;
        int sumInfants = 0;
        int totalPrice = 0;
        int numSeats = 0;

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            if (ticketTypeRequest == null || ticketTypeRequest.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException();
            }

            if (ticketTypeRequest.getTicketType() == ADULT) {
                totalPrice += ADULT_PRICE * ticketTypeRequest.getNoOfTickets();
                numSeats += ticketTypeRequest.getNoOfTickets();
                sumAdults += ticketTypeRequest.getNoOfTickets();
            }
            else if (ticketTypeRequest.getTicketType() == CHILD) {
                totalPrice += CHILD_PRICE * ticketTypeRequest.getNoOfTickets();
                numSeats += ticketTypeRequest.getNoOfTickets();
            }
            else {
                sumInfants += ticketTypeRequest.getNoOfTickets();
            }
        }

        if (sumAdults == 0 || sumInfants > sumAdults) {
            throw new InvalidPurchaseException();
        }
        if (numSeats > MAX_TICKETS) {
            throw new InvalidPurchaseException();
        }

        ticketPaymentService.makePayment(accountId, totalPrice);
        seatReservationService.reserveSeat(accountId, numSeats);
    }
}
