LOCK TABLES `adapter_virtual_ipv6` WRITE;
/*!40000 ALTER TABLE `adapter_virtual_ipv6` DISABLE KEYS */;
ALTER TABLE `adapter_virtual_ipv6` AUTO_INCREMENT = 9000000;
/*!40000 ALTER TABLE `adapter_virtual_ipv6` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `adapter_cluster` WRITE;
/*!40000 ALTER TABLE `adapter_cluster` DISABLE KEYS */;
INSERT INTO `adapter_cluster` (cluster_ipv6_cidr, description, name, password, username) VALUES
('', 'Stingray Cluster 1','core-cluster-1','ce16fed0c9640a17a8b31c7174e64684', 'admin');
/*!40000 ALTER TABLE `adapter_cluster` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `adapter_host` WRITE;
/*!40000 ALTER TABLE `adapter_host` DISABLE KEYS */;
INSERT INTO `adapter_host` (`name`, `host_status`, `endpoint`, `endpoint_active`, `username`, `password`, `cluster_id`) VALUES
('tartarus-1', 'ACTIVE_TARGET', 'https://10.62.164.49:9090/soap', TRUE, 'admin', 'ce16fed0c9640a17a8b31c7174e64684', '1');
/*!40000 ALTER TABLE `adapter_host` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `adapter_virtual_ipv4` WRITE;
/*!40000 ALTER TABLE `adapter_virtual_ipv4` DISABLE KEYS */;
INSERT INTO `adapter_virtual_ipv4` (address, type, is_allocated, last_allocation, last_deallocation, cluster)
                            VALUES ('10.70.176.100', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.101', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.102', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.103', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.104', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.105', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.106', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.107', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.108', 'PUBLIC', FALSE, NULL,NULL ,1)
                                  ,('10.70.176.109', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('192.140.10.25', 'PRIVATE', FALSE, NULL, NULL ,1)
                                  ,('192.140.10.26', 'PRIVATE', FALSE, NULL, NULL ,1)
                                  ,('192.140.10.27', 'PRIVATE', FALSE, NULL, NULL ,1)
                                  ,('192.140.10.28', 'PRIVATE', FALSE, NULL,NULL ,1)
                                  ,('192.140.10.29', 'PRIVATE', FALSE, NULL, NULL ,1)
                                  ,('192.140.10.30', 'PRIVATE', FALSE, NULL, NULL ,1)
                                  ,('192.140.10.31', 'PRIVATE', FALSE, NULL, NULL ,1)
                                  ,('192.140.10.32', 'PRIVATE', FALSE, NULL, NULL ,1);
/*!40000 ALTER TABLE `adapter_virtual_ipv4` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `limit_type`
--

--LOCK TABLES `adapter_limit_type` WRITE;
--/*!40000 ALTER TABLE `adapter_limit_type` DISABLE KEYS */;
--INSERT INTO `adapter_limit_type` VALUES ('CORE', 'LOADBALANCER_LIMIT',25,'Max number of load balancers for an account');
--INSERT INTO `adapter_limit_type` VALUES ('CORE', 'NODE_LIMIT',25,'Max number of nodes for a load balancer');
--INSERT INTO `adapter_limit_type` VALUES ('CORE', 'IPV6_LIMIT',25,'Max number of IPv6 addresses for a load balancer');
--INSERT INTO `adapter_limit_type` VALUES ('CORE', 'BATCH_DELETE_LIMIT',10,'Max number of items that can be deleted for batch delete operations');
--/*!40000 ALTER TABLE `adapter_limit_type` ENABLE KEYS */;
--UNLOCK TABLES;
--
LOCK TABLES `host_status` WRITE;
CREATE TABLE `host_status` (`name` varchar(32) NOT NULL,`description` varchar(128) NOT NULL,PRIMARY KEY  (`name`)) ;
insert into host_status(name, description) values("ACTIVE", "Indicates that the host is in the active status");
insert into host_status(name, description) values("ACTIVE_TARGET", "Indicates that the host is in the active target status");
insert into host_status(name, description) values("BURN_IN", "Indicates that the host is in the burn-in status");
insert into host_status(name, description) values("FAILOVER", "Indicates that the host is in the failover status");
insert into host_status(name, description) values("OFFLINE", "Indicates that the host is in the offline status");
UNLOCK TABLES;


