import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class TicketServiceTest {
    TicketPaymentService paymentServiceMock;
    SeatReservationService seatReservationServiceMock;
    TicketService ticketService;

    @BeforeEach
    public void setup() {
        paymentServiceMock = mock(TicketPaymentService.class);
        seatReservationServiceMock = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(paymentServiceMock, seatReservationServiceMock);
    }

    private static Stream<Long> invalidAccountIds() {
        return Stream.of(null, 0L, -1L);
    }

    @ParameterizedTest
    @MethodSource("invalidAccountIds")
    public void purchaseTickets_whenGivenInvalidAccountId_throwsInvalidPurchaseException(Long accountId) {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(accountId, new TicketTypeRequest(ADULT, 15))
        );
        verifyNoInteractions(paymentServiceMock, seatReservationServiceMock);
    }

    @Test
    public void purchaseTickets_whenGivenNullTicketArray_throwsInvalidPurchaseException() {
        assertThrows(InvalidPurchaseException.class, () ->
            ticketService.purchaseTickets(1L, (TicketTypeRequest[]) null)
        );
        verifyNoInteractions(paymentServiceMock, seatReservationServiceMock);
    }

    @Test
    public void purchaseTickets_whenGivenEmptyTicketArray_throwsInvalidPurchaseException() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L)
        );
        verifyNoInteractions(paymentServiceMock, seatReservationServiceMock);
    }

    private static Stream<Arguments> invalidTicketArrayContainingNull() {
        return Stream.of(
                arguments(1L, new TicketTypeRequest[]{
                        null,
                        new TicketTypeRequest(ADULT, 14)
                }),
                arguments(2L, new TicketTypeRequest[]{
                        new TicketTypeRequest(ADULT, 16),
                        null
                }),
                arguments(2L, new TicketTypeRequest[]{
                        null
                })
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTicketArrayContainingNull")
    public void purchaseTickets_whenGivenTicketArrayContainingNull_throwsInvalidPurchaseException(long accountId, TicketTypeRequest[] requests) {
        assertThrows(InvalidPurchaseException.class, () ->
            ticketService.purchaseTickets(accountId, requests)
        );
        verifyNoInteractions(paymentServiceMock, seatReservationServiceMock);
    }

    private static Stream<Arguments> invalidMoreThanMaxTicketsAreOrdered() {
        return Stream.of(
                arguments(1L, new TicketTypeRequest[]{
                        new TicketTypeRequest(ADULT, 15),
                        new TicketTypeRequest(CHILD, 14)
                }),
                arguments(2L, new TicketTypeRequest[]{
                        new TicketTypeRequest(ADULT, 26)
                })
        );
    }

    @ParameterizedTest
    @MethodSource("invalidMoreThanMaxTicketsAreOrdered")
    public void purchaseTickets_WhenMoreThanMaxTicketsAreOrdered_throwsInvalidPurchaseException(long accountId, TicketTypeRequest[] requests) {

        assertThrows(InvalidPurchaseException.class, () ->
            ticketService.purchaseTickets(accountId,requests)
        );
        verifyNoInteractions(paymentServiceMock, seatReservationServiceMock);
    }

    private static Stream<Arguments> invalidNoAdultTicketsAreOrdered() {
        return Stream.of(
                arguments(1L, new TicketTypeRequest[]{
                        new TicketTypeRequest(CHILD, 1)
                }),
                arguments(2L, new TicketTypeRequest[]{
                        new TicketTypeRequest(INFANT, 1),
                        new TicketTypeRequest(CHILD, 1)
                }),
                arguments(3L, new TicketTypeRequest[]{
                        new TicketTypeRequest(INFANT, 2),
                })
        );
    }

    @ParameterizedTest
    @MethodSource("invalidNoAdultTicketsAreOrdered")
    public void purchaseTickets_whenNoAdultsTicketsAreOrdered_throwsInvalidPurchaseException(long accountId, TicketTypeRequest[] requests) {
        assertThrows(InvalidPurchaseException.class, () ->
            ticketService.purchaseTickets(accountId,requests)
        );
        verifyNoInteractions(paymentServiceMock, seatReservationServiceMock);
    }

    private static Stream<Arguments> invalidMoreInfantThanAdultTicketsAreOrdered() {
        return Stream.of(
                arguments(1L, new TicketTypeRequest[]{
                        new TicketTypeRequest(INFANT, 1)
                }),
                arguments(2L, new TicketTypeRequest[]{
                        new TicketTypeRequest(INFANT, 2),
                        new TicketTypeRequest(ADULT, 1)
                }),
                arguments(3L, new TicketTypeRequest[]{
                        new TicketTypeRequest(INFANT, 2),
                        new TicketTypeRequest(CHILD, 1),
                        new TicketTypeRequest(ADULT, 1)
                })
        );
    }

    @ParameterizedTest
    @MethodSource("invalidMoreInfantThanAdultTicketsAreOrdered")
    public void purchaseTickets_whenMoreInfantTicketsThanAdultTicketsAreOrdered_throwsInvalidPurchaseException(long accountId, TicketTypeRequest[] requests) {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(accountId,requests)
        );
        verifyNoInteractions(paymentServiceMock, seatReservationServiceMock);
    }

    private static Stream<Arguments> invalidZeroOrLessNumberOfTicketsAreOrderedInAnyTicketRequest() {
        return Stream.of(
                arguments(1L, new TicketTypeRequest[]{
                        new TicketTypeRequest(ADULT, 1),
                        new TicketTypeRequest(ADULT, -2)
                }),
                arguments(1L, new TicketTypeRequest[]{
                        new TicketTypeRequest(ADULT, 0),
                        new TicketTypeRequest(ADULT, 4)
                }),
                arguments(1L, new TicketTypeRequest[]{
                        new TicketTypeRequest(CHILD, 0),
                        new TicketTypeRequest(ADULT, 4)
                }),
                arguments(1L, new TicketTypeRequest[]{
                        new TicketTypeRequest(CHILD, -1),
                        new TicketTypeRequest(ADULT, 4)
                }),
                arguments(1L, new TicketTypeRequest[]{
                        new TicketTypeRequest(INFANT, 0),
                        new TicketTypeRequest(ADULT, 4)
                }),
                arguments(1L, new TicketTypeRequest[]{
                        new TicketTypeRequest(INFANT, -1),
                        new TicketTypeRequest(ADULT, 4)
                })
        );
    }
    @ParameterizedTest
    @MethodSource("invalidZeroOrLessNumberOfTicketsAreOrderedInAnyTicketRequest")
    public void purchaseTickets_whenGivenTicketRequestWithZeroOrLessNumberOfTicketsToOrder_throwsInvalidPurchaseException(long accountId, TicketTypeRequest[] requests) {
        assertThrows(InvalidPurchaseException.class, () ->
            ticketService.purchaseTickets(accountId,requests)
        );
        verifyNoInteractions(paymentServiceMock, seatReservationServiceMock);
    }

    private static Stream<Arguments> validTickets() {
        return Stream.of(
                arguments(1L, 375, 15, new TicketTypeRequest[]{
                        new TicketTypeRequest(ADULT, 15),
                }),
                arguments(1L, 625, 25, new TicketTypeRequest[]{
                        new TicketTypeRequest(ADULT, 25),
                }),
                arguments(1L, 625, 25, new TicketTypeRequest[]{
                        new TicketTypeRequest(ADULT, 25),
                        new TicketTypeRequest(INFANT, 10),
                }),
                arguments(1L, 325, 15, new TicketTypeRequest[]{
                        new TicketTypeRequest(ADULT, 10),
                        new TicketTypeRequest(CHILD, 5),
                }),
                arguments(1L, 250, 10, new TicketTypeRequest[]{
                        new TicketTypeRequest(ADULT, 10),
                        new TicketTypeRequest(INFANT, 10),
                }),
                arguments(1L, 250, 10, new TicketTypeRequest[]{
                        new TicketTypeRequest(ADULT, 5),
                        new TicketTypeRequest(ADULT, 5),
                        new TicketTypeRequest(INFANT, 10),
                }),
                arguments(1L, 400, 20, new TicketTypeRequest[]{
                        new TicketTypeRequest(ADULT, 10),
                        new TicketTypeRequest(CHILD, 10),
                        new TicketTypeRequest(INFANT, 10),
                })
        );
    }
    @ParameterizedTest
    @MethodSource("validTickets")
    public void purchaseTickets_whenValidTicketsAreOrdered_CorrectlyCallsPaymentServiceAndSeatReservationService(long accountId, int expectedPrice, int expectedSeat, TicketTypeRequest[] requests) {
        ticketService.purchaseTickets(accountId,requests);
        verify(paymentServiceMock).makePayment(accountId, expectedPrice);
        verify(seatReservationServiceMock).reserveSeat(accountId, expectedSeat);
        verifyNoMoreInteractions(paymentServiceMock, seatReservationServiceMock);
    }
}
