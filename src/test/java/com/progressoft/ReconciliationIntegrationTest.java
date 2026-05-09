package com.progressoft;

import com.progressoft.entity.ImportBatch;
import com.progressoft.repository.BatchRepository;
import com.progressoft.repository.RequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ReconciliationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Test
    public void testFullCsvImportFlow() throws Exception {
        String csvContent = "request_id,customer_reference,identifier_1,identifier_2,identifier_3,submitted_at\n" +
                "REQ-101,CUST-1,OM040270000001234567001,03151234567001,1234567,2026-04-27T10:15:30Z\n" +
                "REQ-102,CUST-2,INVALID,0000000,GB29NWBK60161331926819,2026-04-27T10:18:30Z\n" +
                "REQ-101,CUST-1,OM040270000001234567001,03151234567001,1234567,2026-04-27T10:19:30Z\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/account-checks/import").file(file))
                .andExpect(status().isOk());

        // verify batch persistence
        assertThat(batchRepository.findAll()).hasSize(1);
        ImportBatch batch = batchRepository.findAll().get(0);
        assertThat(batch.getTotalRows()).isEqualTo(3);
        assertThat(batch.getImportedRows()).isEqualTo(1);
        assertThat(batch.getInvalidRows()).isEqualTo(1);
        assertThat(batch.getDuplicateRows()).isEqualTo(1);

        // verify request persistence
        assertThat(requestRepository.findAll()).hasSize(2);

        assertThat(requestRepository.findByRequestId("REQ-101")).isPresent();
        assertThat(requestRepository.findByRequestId("REQ-101").get().getStatus()).isEqualTo("IMPORTED");

        assertThat(requestRepository.findByRequestId("REQ-102")).isPresent();
        assertThat(requestRepository.findByRequestId("REQ-102").get().getStatus()).isEqualTo("INVALID");
    }
}
