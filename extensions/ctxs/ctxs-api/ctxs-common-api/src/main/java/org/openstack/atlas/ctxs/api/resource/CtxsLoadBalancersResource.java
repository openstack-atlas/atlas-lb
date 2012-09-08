package org.openstack.atlas.ctxs.api.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.service.domain.operation.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import org.openstack.atlas.api.validation.validator.LoadBalancerValidator;
import org.openstack.atlas.core.api.v1.LoadBalancers;

import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.LoadBalancerService;
import org.openstack.atlas.service.domain.service.VirtualIpService;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.operation.CoreOperation;

import org.openstack.atlas.api.v1.extensions.ctxs.CertificateRef;
import org.openstack.atlas.ctxs.service.domain.entity.CtxsLoadBalancer;
import org.openstack.atlas.api.resource.LoadBalancerResource;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import javax.xml.bind.JAXBElement;

import static javax.ws.rs.core.MediaType.*;


@Primary
@Controller
@Scope("request")
public class CtxsLoadBalancersResource extends org.openstack.atlas.api.resource.LoadBalancersResource  {
                                        
    public static Log LOG = LogFactory.getLog(CtxsLoadBalancersResource.class.getName());

    @Override
    public Response create(LoadBalancer _loadBalancer) {

        ValidatorResult result = validator.validate(_loadBalancer, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }
        try {
            CtxsLoadBalancer ctxsLoadBalancer = dozerMapper.map(_loadBalancer, CtxsLoadBalancer.class);
            ctxsLoadBalancer.setAccountId(accountId);

            //This call should be moved somewhere else
            virtualIpService.addAccountRecord(accountId);

            CtxsLoadBalancer newlyCreatedLb = (CtxsLoadBalancer) loadbalancerService.create(ctxsLoadBalancer);
            MessageDataContainer msg = new MessageDataContainer();
            msg.setLoadBalancer(newlyCreatedLb);
            asyncService.callAsyncLoadBalancingOperation(CoreOperation.CREATE_LOADBALANCER, msg);
            LoadBalancer returnLoadBalancer = dozerMapper.map(newlyCreatedLb, LoadBalancer.class);
            return Response.status(Response.Status.ACCEPTED).entity(returnLoadBalancer).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }
}
