/**
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

package fr.gouv.vitam.storage.engine.server.distribution.impl;

import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.storage.driver.Connection;
import fr.gouv.vitam.storage.driver.Driver;
import fr.gouv.vitam.storage.driver.exception.StorageDriverException;
import fr.gouv.vitam.storage.driver.model.StorageGetResult;
import fr.gouv.vitam.storage.driver.model.StorageObjectRequest;
import fr.gouv.vitam.storage.engine.common.exception.StorageException;
import fr.gouv.vitam.storage.engine.common.referential.StorageOfferProvider;
import fr.gouv.vitam.storage.engine.common.referential.StorageOfferProviderFactory;
import fr.gouv.vitam.storage.engine.common.referential.model.OfferReference;
import fr.gouv.vitam.storage.engine.common.referential.model.StorageOffer;

/**
 * Thread Future used to send stream to one offer
 */
public class ExportThread implements Callable<ThreadResponseData> {
    public static final String TIMEOUT_TEST = "timeoutTest";
    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(ExportThread.class);

    private static final StorageOfferProvider OFFER_PROVIDER = StorageOfferProviderFactory.getDefaultProvider();

    private final Driver driver;
    private final OfferReference offerReference;
    private final StorageObjectRequest request;

    private static boolean IS_JUNIT_MODE = false;

    /**
     * Default constructor
     *
     * @param driver         thre diver
     * @param offerReference the offer reference to put object
     * @param request        the request to put object
     * @param globalDigest   the globalDigest associated with the stream
     */
    public ExportThread(Driver driver, OfferReference offerReference, StorageObjectRequest request) {
        ParametersChecker.checkParameter("Driver cannot be null", driver);
        ParametersChecker.checkParameter("OfferReference cannot be null", offerReference);
        ParametersChecker.checkParameter("PutObjectRequest cannot be null", request);
        this.driver = driver;
        this.offerReference = offerReference;
        this.request = request;
    }

    /**
     * Allow to check timeout in Junit
     *
     * @param mode if true allow to implement timeout using GUID to "timeoutTest"
     */
    public static void setJunitMode(boolean mode) {
        IS_JUNIT_MODE = mode;
    }

    @Override
    public ThreadResponseData call()
        throws StorageException, StorageDriverException, InterruptedException {
        if (IS_JUNIT_MODE && request.getGuid().equals(TIMEOUT_TEST) && request.getTenantId() == 0) {
            LOGGER.info("Sleep for Junit test");
            Thread.sleep(100);
            return null;
        }
        LOGGER.debug(request.toString());
        final StorageOffer offer = OFFER_PROVIDER.getStorageOffer(offerReference.getId());
        ThreadResponseData response;
        try (Connection connection = driver.connect(offer.getId())) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            // FIXME: 03/04/19 manage exceptions
            StorageObjectRequest getObjectRequest = new StorageObjectRequest(request.getTenantId(), request.getType(),
                request.getGuid());

            StorageGetResult getObjectResult = connection.getAsyncObject(getObjectRequest);
            LOGGER.debug(getObjectResult.toString());
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            response = new ThreadResponseData(
                getObjectResult,
                Response.Status.ACCEPTED, request.getGuid());
        }
        return response;
    }
}