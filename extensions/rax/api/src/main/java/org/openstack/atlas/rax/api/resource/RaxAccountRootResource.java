package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.AccountRootResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Primary
@Controller
@Scope("request")
public class RaxAccountRootResource extends AccountRootResource {
}
