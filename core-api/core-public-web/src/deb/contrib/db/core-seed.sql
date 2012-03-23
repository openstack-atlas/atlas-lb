LOCK TABLES `virtual_ipv6` WRITE;
/*!40000 ALTER TABLE `virtual_ipv6` DISABLE KEYS */;
ALTER TABLE `virtual_ipv6` AUTO_INCREMENT = 9000000;
/*!40000 ALTER TABLE `virtual_ipv6` ENABLE KEYS */;
UNLOCK TABLES;



--
-- Dumping data for table `limit_type`
--

LOCK TABLES `limit_type` WRITE;
/*!40000 ALTER TABLE `limit_type` DISABLE KEYS */;
INSERT INTO `limit_type` VALUES ('CORE', 'LOADBALANCER_LIMIT',25,'Max number of load balancers for an account');
INSERT INTO `limit_type` VALUES ('CORE', 'NODE_LIMIT',25,'Max number of nodes for a load balancer');
INSERT INTO `limit_type` VALUES ('CORE', 'IPV6_LIMIT',25,'Max number of IPv6 addresses for a load balancer');
INSERT INTO `limit_type` VALUES ('CORE', 'BATCH_DELETE_LIMIT',10,'Max number of items that can be deleted for batch delete operations');
/*!40000 ALTER TABLE `limit_type` ENABLE KEYS */;
UNLOCK TABLES;

CREATE TABLE `host_status` (`name` varchar(32) NOT NULL,`description` varchar(128) NOT NULL,PRIMARY KEY  (`name`)) ; 
insert into host_status(name, description) values("ACTIVE", "Indicates that the host is in the active status");
insert into host_status(name, description) values("ACTIVE_TARGET", "Indicates that the host is in the active target status");
insert into host_status(name, description) values("BURN_IN", "Indicates that the host is in the burn-in status");
insert into host_status(name, description) values("FAILOVER", "Indicates that the host is in the failover status");
insert into host_status(name, description) values("OFFLINE", "Indicates that the host is in the offline status");


