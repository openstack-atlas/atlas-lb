/* SQL statements must be on one line for hibernate to load properly :\ */


INSERT INTO `limit_type` VALUES ('CORE', 'LOADBALANCER_LIMIT',25,'Max number of load balancers for an account');
INSERT INTO `limit_type` VALUES ('CORE', 'NODE_LIMIT',25,'Max number of nodes for a load balancer');
INSERT INTO `limit_type` VALUES ('CORE', 'IPV6_LIMIT',25,'Max number of IPv6 addresses for a load balancer');
INSERT INTO `limit_type` VALUES ('CORE', 'BATCH_DELETE_LIMIT',10,'Max number of items that can be deleted for batch delete operations');
