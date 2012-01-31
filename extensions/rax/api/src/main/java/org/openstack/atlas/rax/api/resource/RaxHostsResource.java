package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.v1.extensions.rax.Host;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.rax.api.validation.validator.RaxHostValidator;
import org.openstack.atlas.rax.domain.entity.RaxHost;
import org.openstack.atlas.rax.domain.service.RaxHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Controller
@Scope("request")
public class RaxHostsResource extends CommonDependencyProvider {

    @Autowired
    protected RaxHostValidator validator;

    @Autowired
    protected RaxHostService raxHostService;

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response create(Host _host) {
        ValidatorResult result = validator.validate(_host, HttpRequestType.POST);
        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }
        try {
            RaxHost raxHost = dozerMapper.map(_host, RaxHost.class);
            raxHostService.create(raxHost);

            return Response.status(Response.Status.ACCEPTED).entity(dozerMapper.map(raxHost, Host.class)).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }
}
