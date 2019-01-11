/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.worker.core.plugin.preservation;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.batch.report.model.PreservationStatus;
import fr.gouv.vitam.common.format.identification.model.FormatIdentifierResponse;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.ActionTypePreservation;
import fr.gouv.vitam.common.model.objectgroup.DbObjectGroupModel;
import fr.gouv.vitam.common.model.objectgroup.DbQualifiersModel;
import fr.gouv.vitam.common.thread.RunWithCustomExecutorRule;
import fr.gouv.vitam.common.thread.VitamThreadPoolExecutor;
import fr.gouv.vitam.metadata.client.MetaDataClient;
import fr.gouv.vitam.metadata.client.MetaDataClientFactory;
import fr.gouv.vitam.storage.engine.common.model.response.StoredInfoResult;
import fr.gouv.vitam.worker.core.plugin.preservation.model.OutputPreservation;
import fr.gouv.vitam.worker.core.plugin.preservation.model.WorkflowBatchResult;
import fr.gouv.vitam.worker.core.plugin.preservation.model.WorkflowBatchResult.OutputExtra;
import fr.gouv.vitam.worker.core.plugin.preservation.model.WorkflowBatchResults;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static fr.gouv.vitam.common.model.StatusCode.OK;
import static fr.gouv.vitam.worker.core.plugin.preservation.TestWorkerParameter.TestWorkerParameterBuilder.workerParameterBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

public class PreservationUpdateObjectGroupPluginTest {
    private final String GOT_ID = "GOT_ID";

    private final TestWorkerParameter parameter = workerParameterBuilder().withContainerName("CONTAINER_NAME_TEST")
        .withRequestId("REQUEST_ID_TEST")
        .build();

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Rule
    public RunWithCustomExecutorRule runInThread = new RunWithCustomExecutorRule(VitamThreadPoolExecutor.getDefaultExecutor());

    @Mock
    private MetaDataClientFactory metaDataClientFactory;

    @Mock
    private MetaDataClient metaDataClient;

    private PreservationUpdateObjectGroupPlugin plugin;

    private final TestHandlerIO handlerIO = new TestHandlerIO();

    @Before
    public void setUp() throws Exception {
        given(metaDataClientFactory.getClient()).willReturn(metaDataClient);
        parameter.setObjectNameList(Collections.singletonList(GOT_ID));
        plugin = new PreservationUpdateObjectGroupPlugin(metaDataClientFactory);

        FormatIdentifierResponse format = new FormatIdentifierResponse("Plain Text File", "text/plain", "x-fmt/111", "");
        StoredInfoResult value = new StoredInfoResult();
        OutputPreservation output = new OutputPreservation();
        output.setStatus(PreservationStatus.OK);
        output.setAction(ActionTypePreservation.GENERATE);
        OutputExtra outputExtra = new OutputExtra(
            output,
            "binaryGUID",
            Optional.of(12L),
            Optional.of("hash"),
            Optional.of(format),
            Optional.of(value),
            Optional.empty()
        );

        WorkflowBatchResult batchResult =
            WorkflowBatchResult.of("gotId", "unitId", "BinaryMaster", "requestId", Collections.singletonList(outputExtra),
                "BinaryMaster");
        WorkflowBatchResults batchResults = new WorkflowBatchResults(Paths.get("tmp"), Collections.singletonList(batchResult));

        handlerIO.addOutputResult(0, batchResults);
        handlerIO.setInputs(batchResults);
    }

    @Test
    public void should_update_objectGroup_with_new_binary() throws Exception {
        // Given
        InputStream objectGroupStream = Object.class.getResourceAsStream("/preservation/objectGroupDslResponse.json");
        JsonNode objectGroupNode = JsonHandler.getFromInputStream(objectGroupStream);
        RequestResponse<JsonNode> responseOK =
            new RequestResponseOK<JsonNode>().addResult(objectGroupNode).setHttpCode(Response.Status.OK.getStatusCode());
        given(metaDataClient.getObjectGroupByIdRaw(ArgumentMatchers.any())).willReturn(responseOK);

        // When
        List<ItemStatus> itemStatuses = plugin.executeList(parameter, handlerIO);

        // Then
        assertThat(itemStatuses).extracting(ItemStatus::getGlobalStatus).containsOnly(OK);
    }

}
