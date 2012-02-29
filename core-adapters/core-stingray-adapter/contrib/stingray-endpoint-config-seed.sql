LOCK TABLES `virtual_ipv6` WRITE;
/*!40000 ALTER TABLE `virtual_ipv6` DISABLE KEYS */;
ALTER TABLE `virtual_ipv6` AUTO_INCREMENT = 9000000;
/*!40000 ALTER TABLE `virtual_ipv6` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `cluster` WRITE;
/*!40000 ALTER TABLE `cluster` DISABLE KEYS */;
INSERT INTO `cluster` VALUES ('CORE', 1,NULL,'Stingray Cluster 1','core-cluster-1','ce16fed0c9640a17a8b31c7174e64684','admin');
/*!40000 ALTER TABLE `cluster` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `host` WRITE;
/*!40000 ALTER TABLE `host` DISABLE KEYS */;
INSERT INTO `host` (`vendor`, `id`, `name`, `host_status`, `endpoint`, `endpoint_active`, `username`, `password`, `cluster_id`, `ipv6_service_net`, `ipv4_service_net`, `ipv4_public`, `ipv6_public`) VALUES ('CORE', 1, 'stingray1.localdomain', 'ACTIVE_TARGET', 'https://192.168.37.10:9090/soap', TRUE, 'admin','ce16fed0c9640a17a8b31c7174e64684', '1', NULL, '192.168.37.10', '192.168.37.10', NULL),
('CORE', 2, 'stingray2.localdomain', 'ACTIVE_TARGET', 'https://192.168.37.11:9090/soap', TRUE, 'admin', 'ce16fed0c9640a17a8b31c7174e64684', '1', NULL, '192.168.37.11', '192.168.37.11', NULL);
/*!40000 ALTER TABLE `host` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `virtual_ipv4` WRITE;
/*!40000 ALTER TABLE `virtual_ipv4` DISABLE KEYS */;
INSERT INTO `virtual_ipv4` VALUES ('CORE', 21,'192.168.37.121', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 22,'192.168.37.122', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 23,'192.168.37.123', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 24,'192.168.37.124', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 25,'192.168.37.125', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 26,'192.168.37.126', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 27,'192.168.37.127', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 28,'192.168.37.128', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 29,'192.168.37.129', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 30,'192.168.37.120', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 31,'192.168.37.111', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 32,'192.168.37.112', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 33,'192.168.37.113', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 34,'192.168.37.114', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 35,'192.168.37.115', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 36,'192.168.37.116', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 37,'192.168.37.117', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 38,'192.168.37.118', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 39,'192.168.37.119', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 40,'192.168.37.200', FALSE, NULL, NULL ,'PRIVATE',1);
/*!40000 ALTER TABLE `virtual_ipv4` ENABLE KEYS */;
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

LOCK TABLES `host_status` WRITE;
CREATE TABLE `host_status` (`name` varchar(32) NOT NULL,`description` varchar(128) NOT NULL,PRIMARY KEY  (`name`)) ;
insert into host_status(name, description) values("ACTIVE", "Indicates that the host is in the active status");
insert into host_status(name, description) values("ACTIVE_TARGET", "Indicates that the host is in the active target status");
insert into host_status(name, description) values("BURN_IN", "Indicates that the host is in the burn-in status");
insert into host_status(name, description) values("FAILOVER", "Indicates that the host is in the failover status");
insert into host_status(name, description) values("OFFLINE", "Indicates that the host is in the offline status");
UNLOCK TABLES;


