package com.vsms.servicerequestservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsms.servicerequestservice.dto.request.PaymentDTO;
import com.vsms.servicerequestservice.dto.response.InvoiceResponse;
import com.vsms.servicerequestservice.dto.response.RevenueStats;
import com.vsms.servicerequestservice.enums.InvoiceStatus;
import com.vsms.servicerequestservice.enums.PaymentMethod;
import com.vsms.servicerequestservice.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private InvoiceController controller;

    private InvoiceResponse testResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testResponse = InvoiceResponse.builder()
                .id(1)
                .invoiceNumber("INV-2026-0001")
                .serviceRequestId(1)
                .customerId(1)
                .laborCost(BigDecimal.valueOf(300))
                .partsCost(BigDecimal.valueOf(200))
                .totalAmount(BigDecimal.valueOf(500))
                .status(InvoiceStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllInvoices_Success() throws Exception {
        when(invoiceService.getAllInvoices()).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(invoiceService, times(1)).getAllInvoices();
    }

    @Test
    void getMyInvoices_Success() throws Exception {
        when(invoiceService.getMyInvoices(1)).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/invoices/my")
                .param("customerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(invoiceService, times(1)).getMyInvoices(1);
    }

    @Test
    void getUnpaidInvoices_Success() throws Exception {
        when(invoiceService.getUnpaidInvoices()).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/invoices/unpaid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(invoiceService, times(1)).getUnpaidInvoices();
    }

    @Test
    void payInvoice_Success() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentMethod(PaymentMethod.CARD);

        when(invoiceService.payInvoice(eq(1), any(PaymentDTO.class))).thenReturn(testResponse);

        mockMvc.perform(put("/api/invoices/1/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(invoiceService, times(1)).payInvoice(eq(1), any(PaymentDTO.class));
    }

    @Test
    void getRevenueStats_Success() throws Exception {
        RevenueStats stats = RevenueStats.builder()
                .totalInvoices(10)
                .paidInvoices(5)
                .unpaidInvoices(5)
                .totalRevenue(BigDecimal.valueOf(5000))
                .build();

        when(invoiceService.getRevenueStats()).thenReturn(stats);

        mockMvc.perform(get("/api/invoices/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalInvoices").value(10));

        verify(invoiceService, times(1)).getRevenueStats();
    }

    @Test
    void generateInvoice_Success() throws Exception {
        when(invoiceService.generateInvoice(1)).thenReturn(testResponse);

        mockMvc.perform(post("/api/invoices/generate/1"))
                .andExpect(status().isCreated());

        verify(invoiceService, times(1)).generateInvoice(1);
    }
}
