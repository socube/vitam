/*******************************************************************************
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
 *******************************************************************************/
package fr.gouv.vitam.functional.administration.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fr.gouv.vitam.functional.administration.common.exception.*;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.thread.RunWithCustomExecutor;
import fr.gouv.vitam.common.thread.RunWithCustomExecutorRule;
import fr.gouv.vitam.common.thread.VitamThreadPoolExecutor;
import fr.gouv.vitam.common.thread.VitamThreadUtils;
import fr.gouv.vitam.functional.administration.client.model.AccessionRegisterDetailModel;


public class AdminManagementClientMockTest {

    AdminManagementClientMock client = new AdminManagementClientMock();
    private static final Integer TENANT_ID = 0;
    InputStream stream;

    @Rule
    public RunWithCustomExecutorRule runInThread =
        new RunWithCustomExecutorRule(VitamThreadPoolExecutor.getDefaultExecutor());

    @Test
    public void givenClientMockWhenAndInputXMLOKThenReturnOK() throws FileFormatException, FileNotFoundException {
        stream = PropertiesUtils.getResourceAsStream("FF-vitam.xml");
        client.checkFormat(stream);
    }

    @Test
    public void givenClientMockWhenWhenImportThenReturnOK() throws FileFormatException, FileNotFoundException {
        stream = PropertiesUtils.getResourceAsStream("FF-vitam.xml");
        client.importFormat(stream);
    }

    @Test
    public void getFormatByIDTest() throws InvalidParseOperationException, ReferentialException {
        AdminManagementClientFactory.changeMode(null);
        final AdminManagementClient client = AdminManagementClientFactory.getInstance().getClient();
        assertNotNull(client.getFormatByID("aedqaaaaacaam7mxaaaamakvhiv4rsiaaaaz"));
    }

    @Test
    public void getDocumentTest()
        throws InvalidParseOperationException, ReferentialException, JsonGenerationException, JsonMappingException,
        IOException {
        AdminManagementClientFactory.changeMode(null);
        final AdminManagementClient client = AdminManagementClientFactory.getInstance().getClient();
        final Select select = new Select();
        assertNotNull(client.getFormats(select.getFinalSelect()));
    }

    /****************
     * Rules Manager
     *
     * @throws FileNotFoundException
     *****/
    @Test
    public void givenClientMockWhenAndInputCSVOKThenReturnOK()
        throws FileFormatException, FileRulesException, FileNotFoundException {
        stream = PropertiesUtils.getResourceAsStream("jeu_donnees_OK_regles_CSV.csv");
        client.checkRulesFile(stream);
    }

    @Test
    @RunWithCustomExecutor
    public void givenClientMockWhenWhenImportRuleThenReturnOK()
        throws FileFormatException, FileRulesException, DatabaseConflictException, FileNotFoundException {
        stream = PropertiesUtils.getResourceAsStream("jeu_donnees_OK_regles_CSV.csv");
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        client.importRulesFile(stream);
    }

    @Test
    @RunWithCustomExecutor
    public void getRuleByIDTest() throws InvalidParseOperationException, ReferentialException {
        AdminManagementClientFactory.changeMode(null);
        final AdminManagementClient client = AdminManagementClientFactory.getInstance().getClient();
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        final ObjectNode objectNode = (ObjectNode) client.getRuleByID("APP-00001");
        assertEquals(1, ((ArrayNode) objectNode.get("$results")).size());
        assertEquals("AppraisalRule",
            ((ArrayNode) objectNode.get("$results")).get(0).get("RuleType").asText());
        assertEquals("6", ((ArrayNode) objectNode.get("$results")).get(0).get("RuleDuration").asText());
        assertEquals("Année",
            ((ArrayNode) objectNode.get("$results")).get(0).get("RuleMeasurement").asText());
    }

    @Test
    @RunWithCustomExecutor
    public void getRuleTest()
        throws InvalidParseOperationException, ReferentialException, JsonGenerationException, JsonMappingException,
        IOException {
        AdminManagementClientFactory.changeMode(null);
        final AdminManagementClient client = AdminManagementClientFactory.getInstance().getClient();
        final Select select = new Select();
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        assertNotNull(client.getRules(select.getFinalSelect()));
    }

    @Test
    @RunWithCustomExecutor
    public void givenClientMockWhenCreateAccessionRegister() throws Exception {
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        client.createorUpdateAccessionRegister(new AccessionRegisterDetailModel());
    }

    @Test
    public void getFundRegisterTest()
        throws InvalidParseOperationException, ReferentialException, JsonGenerationException, JsonMappingException,
        IOException {
        AdminManagementClientFactory.changeMode(null);
        final AdminManagementClient client = AdminManagementClientFactory.getInstance().getClient();
        final Select select = new Select();
        assertNotNull(client.getAccessionRegister(select.getFinalSelect()));
    }

    @Test
    public void getAccessionRegisterDetailTest()
        throws InvalidParseOperationException, ReferentialException, JsonGenerationException, JsonMappingException,
        IOException {
        AdminManagementClientFactory.changeMode(null);
        final AdminManagementClient client = AdminManagementClientFactory.getInstance().getClient();
        final Select select = new Select();
        final RequestResponse detailResponse = client.getAccessionRegisterDetail(select.getFinalSelect());

        if (detailResponse.isOk()) {
            RequestResponseOK<AccessionRegisterDetailModel> responseOK =
                (RequestResponseOK<AccessionRegisterDetailModel>) detailResponse;

            assertNotNull(responseOK);

            List<AccessionRegisterDetailModel> results = responseOK.getResults();

            assertNotNull(results);
            assertEquals(1, results.size());
            final AccessionRegisterDetailModel item = results.get(0);
            assertEquals("FRAN_NP_005061", item.getSubmissionAgency());

        }
    }

    @Test
    @RunWithCustomExecutor
    public void givenClientMockWhenImportIngestContracts() throws Exception {
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        RequestResponse resp = client.importIngestContracts(new ArrayList<>());
        assertThat(RequestResponseOK.class).isAssignableFrom(resp.getClass());
        assertThat(((RequestResponseOK)resp).isOk());
        assertThat(((RequestResponseOK)resp).getResults()).hasSize(1);
    }

    @Test(expected = ReferentialNotFoundException.class)
    @RunWithCustomExecutor
    public void givenClientMockWhenfindIngestContractsByFakeID() throws Exception {
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        RequestResponse resp = client.findIngestContractsByID("FakeId");
        assertThat(RequestResponseOK.class).isAssignableFrom(resp.getClass());
        assertThat(((RequestResponseOK)resp).isOk());
        assertThat(((RequestResponseOK)resp).getResults()).hasSize(0);
        throw new ReferentialNotFoundException("Ingest contract not found with id FakeId");
    }


    @Test
    @RunWithCustomExecutor
    public void givenClientMockWhenfindIngestContracts() throws Exception {
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        RequestResponse resp = client.findIngestContracts(JsonHandler.createObjectNode());
        assertThat(RequestResponseOK.class).isAssignableFrom(resp.getClass());
        assertThat(((RequestResponseOK)resp).isOk());
        assertThat(((RequestResponseOK)resp).getResults()).hasSize(1);
    }


    @Test
    @RunWithCustomExecutor
    public void givenClientMockWhenImportAccessContracts() throws Exception {
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        RequestResponse resp = client.importAccessContracts(new ArrayList<>());
        assertThat(RequestResponseOK.class).isAssignableFrom(resp.getClass());
        assertThat(((RequestResponseOK)resp).isOk());
        assertThat(((RequestResponseOK)resp).getResults()).hasSize(1);
    }

    @Test(expected = ReferentialNotFoundException.class)
    @RunWithCustomExecutor
    public void givenClientMockWhenfindAccessContractsByFakeID() throws Exception {
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        RequestResponse resp = client.findAccessContractsByID("FakeId");
        assertThat(RequestResponseOK.class).isAssignableFrom(resp.getClass());
        assertThat(((RequestResponseOK)resp).isOk());
        assertThat(((RequestResponseOK)resp).getResults()).hasSize(0);
        throw new ReferentialNotFoundException("Ingest contract not found with id FakeId");
    }


    @Test
    @RunWithCustomExecutor
    public void givenClientMockWhenfindAccessContracts() throws Exception {
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        RequestResponse resp = client.findAccessContracts(JsonHandler.createObjectNode());
        assertThat(RequestResponseOK.class).isAssignableFrom(resp.getClass());
        assertThat(((RequestResponseOK)resp).isOk());
        assertThat(((RequestResponseOK)resp).getResults()).hasSize(1);
    }

}
