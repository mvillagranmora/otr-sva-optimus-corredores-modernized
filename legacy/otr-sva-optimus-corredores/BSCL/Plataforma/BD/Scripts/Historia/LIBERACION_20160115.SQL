-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencuias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################


--
--
-- LIBERACION SEMANA: 08-01-2016
--

-- 06-01-2015: se agrega en tabla TipoOperacion campo que determinará si la operación corresponde 
-- a las usadas en la pantalla de ingreso/retiro de título (Andrés Céspedes)
	alter table "BSCL.Plataforma.BD::INS.TipoOperacion"
	add ("EsIngresoEgresoTitulo" 		NVARCHAR(1)			default 'N');
	


-- 07-01-2015: se agregan campos en custodia que resultaban necesarios para RF
	alter table "BSCL"."BSCL.Custodia.BD::CUS.Cartera"
	add ("IndicadorDivisibilidad" 		NVARCHAR(3) 	default '' 	not null,  --UNIco, MINimo, STAndar, CUAlquier, INDivisible, NOInforma 
		 "FechaReserva" 				TIMESTAMP		default '0001-01-01' not null,
		 "PlazoEnDias" 					BIGINT 			default '0' not null,
		 "MontoDisponibleActual" 		DECIMAL(26,8)	default '0' not null,
		 "NemoSVS"	 					NVARCHAR(40) 	default '' 	not null
	 );


	alter table "BSCL"."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"	
	add (
	 	 "NemoSVS" 						NVARCHAR(40)	default '' 	not null,
		 "PlazoEnDias" 					BIGINT 			default '0' not null, 
		 "PrecioPromedio"				DECIMAL(26,8) 	default '0' not null,
		 "MontoDisponibleActual"		DECIMAL(26,8) 	default '0' not null,
		 "Fungible" 					NVARCHAR(40) 	default '' 	not null,
	 	 "IndicadorDivisibilidad" 		NVARCHAR(3) 	default '' 	not null,  --UNIco, MINimo, STAndar, CUAlquier, INDivisible, NOInforma 
		 "TasaCompra" 					DECIMAL(26,8) 	default '0' not null,
		 "ValorATasaCompra"				DECIMAL(26,8) 	default '0' not null,
		 "FechaTasaCompra" 				DATE            default '0001-01-01' not null
	);

-- 08-01-2015: se agregan registros requeridos para asignar cobertura para Futuros y Prestamos
	INSERT INTO "BSCL.Custodia.BD::GTA.TipoInstrumentoParaGarantia" ("IdTipoInstrumentoGarantia","IdBolsaTipoInstrumento","IdTipoInstrumentoPermitido","IdEstado")
	VALUES(3,35,'ACN','ACT'); -- FUTURO UF
	 
	INSERT INTO "BSCL.Custodia.BD::GTA.TipoInstrumentoParaGarantia" ("IdTipoInstrumentoGarantia","IdBolsaTipoInstrumento","IdTipoInstrumentoPermitido","IdEstado")
	VALUES(4,35,'ACX','ACT'); -- FUTURO UF
	 
	INSERT INTO "BSCL.Custodia.BD::GTA.TipoInstrumentoParaGarantia" ("IdTipoInstrumentoGarantia","IdBolsaTipoInstrumento","IdTipoInstrumentoPermitido","IdEstado")
	VALUES(5,31,'ACN','ACT'); -- FUTURO DOLAR
	 
	INSERT INTO "BSCL.Custodia.BD::GTA.TipoInstrumentoParaGarantia" ("IdTipoInstrumentoGarantia","IdBolsaTipoInstrumento","IdTipoInstrumentoPermitido","IdEstado")
	VALUES(6,31,'ACX','ACT'); -- FUTURO DOLAR
	 
	INSERT INTO "BSCL.Custodia.BD::GTA.TipoInstrumentoParaGarantia" ("IdTipoInstrumentoGarantia","IdBolsaTipoInstrumento","IdTipoInstrumentoPermitido","IdEstado")
	VALUES(7,36,'ACN','ACT'); -- PRESTAMOS
	 
	INSERT INTO "BSCL.Custodia.BD::GTA.TipoInstrumentoParaGarantia" ("IdTipoInstrumentoGarantia","IdBolsaTipoInstrumento","IdTipoInstrumentoPermitido","IdEstado")
	VALUES(8,36,'ACX','ACT'); -- PRESTAMOS


	--JG: Grupo Activos
	
	--Tipo Grupo Activos.
	INSERT INTO "BSCL.Plataforma.BD::COM.TipoGrupoActivos" ("IdTipoGrupoActivos","Nombre")
	VALUES ('INCLUYENTE','Incluyente');
	INSERT INTO "BSCL.Plataforma.BD::COM.TipoGrupoActivos" ("IdTipoGrupoActivos","Nombre")
	VALUES ('EXCLUYENTE','Excluyente');
	-- Modificación columna.
	ALTER TABLE "BSCL.Plataforma.BD::COM.DetallePerfilProductos" 
	ADD ("IdGrupoActivos" NVARCHAR(40) not null default '');
	-- Inscripción servicio de creación.
	INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
	VALUES(97,'BSCL.Plataforma.BD.Servicios::COM.CrearGrupoActivos',0,'P','');
	
	
	--ACC: campor para RF e IF
	alter table "BSCL"."BSCL.Custodia.BD::CUS.Subyacente"	
	add (
	 	 "NemoSVS" 						NVARCHAR(40)	default '' 	not null,
		 "PlazoEnDias" 					BIGINT 			default '0' not null,
		 "ValorTasaCompra"				DECIMAL(26,8) 	default '0' not null, 
		 "MontoDisponibleActual"		DECIMAL(26,8) 	default '0' not null,
		 "IndicadorDivisibilidad" 		NVARCHAR(3) 	default '' 	not null  --UNIco, MINimo, STAndar, CUAlquier, INDivisible, NOInforma 
	);


--paridad
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (16,'2015-09-26','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (17,'2015-09-26','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (18,'2015-09-26','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (19,'2015-09-27','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (20,'2015-09-27','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (21,'2015-09-27','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (22,'2015-09-28','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (23,'2015-09-28','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (24,'2015-09-28','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (25,'2015-09-29','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (26,'2015-09-29','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (27,'2015-09-29','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (28,'2015-09-30','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (29,'2015-09-30','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (30,'2015-09-30','EUR',1,750,1.5,'ACT');


insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (31,'2015-10-01','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (32,'2015-10-01','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (33,'2015-10-01','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (34,'2015-10-02','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (35,'2015-10-02','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (36,'2015-10-02','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (37,'2015-10-03','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (38,'2015-10-03','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (39,'2015-10-03','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (40,'2015-10-04','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (41,'2015-10-04','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (42,'2015-10-04','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (43,'2015-10-05','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (44,'2015-10-05','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (45,'2015-10-05','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (46,'2015-10-06','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (47,'2015-10-06','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (48,'2015-10-06','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (49,'2015-10-07','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (50,'2015-10-07','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (51,'2015-10-07','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (52,'2015-10-08','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (53,'2015-10-08','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (54,'2015-10-08','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (55,'2015-10-09','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (56,'2015-10-09','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (57,'2015-10-09','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (58,'2015-10-10','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (59,'2015-10-10','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (60,'2015-10-10','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (61,'2015-10-11','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (62,'2015-10-11','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (63,'2015-10-11','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (64,'2015-10-12','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (65,'2015-10-12','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (66,'2015-10-12','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (67,'2015-10-13','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (68,'2015-10-13','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (69,'2015-10-13','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (70,'2015-10-14','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (71,'2015-10-14','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (72,'2015-10-14','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (73,'2015-10-15','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (74,'2015-10-15','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (75,'2015-10-15','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (76,'2015-10-16','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (77,'2015-10-16','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (78,'2015-10-16','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (79,'2015-10-17','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (80,'2015-10-17','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (81,'2015-10-17','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (82,'2015-10-18','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (83,'2015-10-18','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (84,'2015-10-18','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (85,'2015-10-19','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (86,'2015-10-19','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (87,'2015-10-19','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (88,'2015-10-20','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (89,'2015-10-20','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (90,'2015-10-20','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (91,'2015-10-21','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (92,'2015-10-21','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (93,'2015-10-21','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (94,'2015-10-22','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (95,'2015-10-22','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (96,'2015-10-22','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (97,'2015-10-23','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (98,'2015-10-23','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (99,'2015-10-23','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (100,'2015-10-24','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (101,'2015-10-24','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (102,'2015-10-24','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (103,'2015-10-25','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (104,'2015-10-25','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (105,'2015-10-25','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (106,'2015-10-26','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (107,'2015-10-26','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (108,'2015-10-26','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (109,'2015-10-27','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (110,'2015-10-27','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (111,'2015-10-27','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (112,'2015-10-28','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (113,'2015-10-28','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (114,'2015-10-28','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (115,'2015-10-29','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (116,'2015-10-29','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (117,'2015-10-29','EUR',1,750,1.5,'ACT');

insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (118,'2015-10-30','CLP',1,1,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (119,'2015-10-30','USD',1,670,1,'ACT');
insert into bscl."BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" values (120,'2015-10-30','EUR',1,750,1.5,'ACT');


--
--
-- LIBERACION SEMANA: 15-01-2016
--

--11-01-2016
alter table "BSCL.CuentaInversion.BD::CTA.CuentaInversion"
	add ("CuentaInversionDefecto" 		NVARCHAR(1)			default 'N');
	
	
--11-01-2016
alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" add ("IdCarga" BIGINT DEFAULT 0 NOT NULL);
alter table "BSCL.Facturacion.BD::FAC.Comprobante" add ("IdCarga" BIGINT DEFAULT 0 NOT NULL);


-- 12-01-2016
-- Agrega campos a garantía para soporte de RF/IF
	alter table "BSCL"."BSCL.Custodia.BD::GTA.Garantia"
	add (
		 	 "NemoSVS" 						NVARCHAR(40)	default '' 	not null,
			 "PlazoEnDias" 					BIGINT 			default '0' not null, 
			 "PrecioPromedio"				DECIMAL(26,8) 	default '0' not null,
			 "MontoDisponibleActual"		DECIMAL(26,8) 	default '0' not null,
		 	 "IndicadorDivisibilidad" 		NVARCHAR(3) 	default '' 	not null,
		 	 "IdTipoReajuste" 				NVARCHAR(2)   	default ''  not null,
	);
-- 12-01-2016
-- agrega fecha de modificacion para la transaccion	
alter table "BSCL"."BSCL.Orden.BD::ORD.Transaccion" add ("FechaModificacionOperacion" SECONDDATE  NOT NULL DEFAULT '0001-01-01 00:00:00');
-- 13-01-2016
-- se agrega id de carga por migracion
alter table "BSCL.Orden.BD::ORD.CobroAsignacion" add ("IdCarga" BIGINT DEFAULT 0 NOT NULL);
alter table "BSCL.Facturacion.BD::FAC.Comprobante" add ("FolioComprobanteFacturacionMigracion" BIGINT DEFAULT 0 NOT NULL);
alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" add ("FolioComprobanteFacturacionMigracion" BIGINT DEFAULT 0 NOT NULL);
--13-03-2016
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.UsoMonedaFormato"("IdUsoMonedaFormato","IdEmpresa","Codigo","Nombre","IdEstado")  
VALUES('INS',1,'INSTRUMENTO','Formatos para precios de instrumentos','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (51,'CLP','INS','ACN','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (52,'USD','INS','ACX','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (53,'CLP','INS','AO','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (54,'UF','INS','BBC','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (55,'CLP','INS','BBC','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (56,'USD','INS','BBC','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (57,'CLP','INS','BCA','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (58,'USD','INS','BCA','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (59,'UF','INS','BCA','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (60,'UF','INS','BCC','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (61,'USD','INS','BCC','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (62,'CLP','INS','BCC','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (63,'USD','INS','BCD','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (64,'CLP','INS','BCP','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (65,'UF','INS','BCU','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (66,'USD','INS','BCX','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (67,'UF','INS','BEM','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (68,'CLP','INS','BEM','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (69,'USD','INS','BEM','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (70,'CLP','INS','BTP','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (71,'UF','INS','BTU','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (72,'UF','INS','BUS','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (73,'UF','INS','BVL','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (74,'UF','INS','CER','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (75,'CLP','INS','CFM','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (76,'USD','INS','CFX','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (77,'CLP','INS','COR','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (78,'CLP','INS','EMP','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (79,'USD','INS','ETN','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (80,'CLP','INS','ETN','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (81,'USD','INS','ETX','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (82,'CLP','INS','FBC','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (83,'CLP','INS','FDO','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (84,'CLP','INS','FIC','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (85,'CLP','INS','FIP','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (86,'CLP','INS','FUF','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (87,'CLP','INS','LTH','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (88,'USD','INS','LTH','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (89,'UF','INS','LTH','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (90,'USD','INS','PAG','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (91,'USD','INS','PCR','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (92,'USD','INS','PCX','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (93,'UF','INS','PRC','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (94,'USD','INS','PRD','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (95,'CLP','INS','PRT','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (96,'','INS','SIM','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (97,'USD','INS','ST','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (98,'CLP','INS','ST','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (99,'USD','INS','XER','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (100,'USD','INS','ZER','',3,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');


--13-01-2016
-- se agrega monto valorizado para la transaccion
alter table "BSCL.Orden.BD::ORD.Transaccion" add ("MontoValorizado" DECIMAL(26,8) NOT NULL DEFAULT 0);
alter table "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" add ("IdTipoOperacionRelacionada"		NVARCHAR(6) 	default '' 	not null);

delete from  "BSCL.Plataforma.BD::INS.TipoOperacion";
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMACX','COMACX','COMPRA ACCIONES EXTRANJERAS','COMPRA','INAX','S','S','N','N','S','COMPRA','','N','VENACX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENACX','VENACX','VENTA ACCIONES EXTRANJERAS','VENTA','INAX','S','S','N','N','S','VENTA','','N','COMACX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMSIM','COMSIM','COMPRA DE SIMULTÁNEA','COMPRA','INSI','S','S','N','N','S','COMPRA','','N','VENSIM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENSIM','VENSIM','VENTA DE SIMULTÁNEA','VENTA','INSI','S','S','N','N','S','VENTA','','N','COMSIM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMCFI','COMCFI','COMPRA CFI NACIONAL','COMPRA','INCI','S','S','N','N','S','COMPRA','','N','VENCFI');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENCFI','VENCFI','VENTA CFI NACIONAL','VENTA','INCI','S','S','N','N','S','VENTA','','N','COMCFI');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMCFX','COMCFX','COMPRA CFI EXTRANJERO','COMPRA','INCX','S','S','N','N','S','COMPRA','','N','VENCFX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENCFX','VENCFX','VENTA CFI EXTRANJERO','VENTA','INCX','S','S','N','N','S','VENTA','','N','COMCFX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMMON','COMMON','COMPRA MONETARIO','COMPRA','INMO','S','S','N','N','S','COMPRA','','N','VENMON');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENMON','VENMON','VENTA MONETARIO','VENTA','INMO','S','S','N','N','S','VENTA','','N','COMMON');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMETN','COMETN','COMPRA ETF NACIONAL','COMPRA','INEN','S','S','N','N','S','COMPRA','','N','VENETN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENETN','VENETN','VENTA ETF NACIONAL','VENTA','INEN','S','S','N','N','S','VENTA','','N','COMETN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMETX','COMETX','COMPRA ETF EXTRANJERO','COMPRA','INEX','S','S','N','N','S','COMPRA','','N','VENETX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENETX','VENETX','VENTA ETF EXTRANJERO','VENTA','INEX','S','S','N','N','S','VENTA','','N','COMETX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMCFM','COMCFM','COMPRA CFM','COMPRA','INCM','S','S','N','N','S','COMPRA','','N','VENCFM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENCFM','VENCFM','VENTA CFM','VENTA','INCM','S','S','N','N','S','VENTA','','N','COMCFM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMPAC','COMPAC','COMPRA CON PACTO','COMPRA','MERF','S','N','N','N','N','COMPRA','','N','VENPAC');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENPAC','VENPAC','VENTA CON PACTO','VENTA','MERF','S','N','N','N','N','VENTA','','N','COMPAC');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMTER','COMTER','COMPRA A TERMINO','COMPRA','MERF','S','N','N','N','N','COMPRA','','N','VENTER');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENTER','VENTER','VENTA A TERMINO','VENTA','MERF','S','N','N','N','N','VENTA','','N','COMTER');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('INGTIT','INGTIT','INGRESO DE TITULO','INGRESO','CUST','N','N','N','N','N','INGRESO','','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('EGRTIT','EGRTIT','EGRESO DE TITULO','EGRESO','CUST','N','N','N','N','N','EGRESO','','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('TRATIT','TRATIT','TRASPASO DE TITULOS','TRASPASO','CUST','N','N','N','N','N','TRASPASO','','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('PRESTA','PRESTA','PRESTAMO','PRESTAMO','INAC','N','N','N','N','N','PRESTAMO','','N','EMPRES');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('EMPRES','EMPRES','EMPRESTITO','EMPRESTITO','INAC','N','N','N','N','N','EMPRESTITO','','N','PRESTA');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMFWD','COMFWD','COMPRA DE FORWARD','COMPRA','MEFW','S','N','N','N','N','COMPRA','','N','VENFWD');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENFWD','VENFWD','VENTA DE FORWARD','VENTA','MEFW','S','N','N','N','N','VENTA','','N','COMFWD');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMSPT','COMSPT','COMPRA SPOT','COMPRA','MEME','S','N','N','N','N','COMPRA','','N','VENSPT');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENSPT','VENSPT','VENTA SPOT','VENTA','MEME','S','N','N','N','N','VENTA','','N','COMSPT');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('APORFM','APORFM','APORTE FONDO MUTUO','APORTE','INFM','N','N','N','N','N','APORTE','','N','RESCFM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('RESCFM','RESCFM','RESCATE FONDO MUTUO','RESCATE','INFM','N','N','N','N','N','RESCATE','','N','APORFM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('CARMAN','CARMAN','CARGO MANUAL','CARGO','CTIN','N','N','N','N','N','CARGO','','N','ABNMAN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('ABNMAN','ABNMAN','ABONO MANUAL','ABONO','CTIN','N','N','N','N','N','ABONO','','N','CARMAN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('CARTRA','CARTRA','CARGO TRASPASO','CARGO','CTIN','N','N','N','N','N','CARGO','','N','ABNTRA');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMIIF','COMIIF','COMPRA INTERMEDIACION FINANCIERA','COMPRA','INIF','S','S','N','N','S','COMPRA','','N','VENIIF');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENIIF','VENIIF','VENTA INTERMEDIACION FINANCIERA','VENTA','INIF','S','S','N','N','S','VENTA','','N','COMIIF');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMRF','COMRF','COMPRA MERCADO RENTA FIJA','COMPRA','INRF','S','S','N','N','S','COMPRA','','N','VENRF');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENRF','VENRF','VENTA MERCADO RENTA FIJA','VENTA','INRF','S','S','N','N','S','VENTA','','N','COMRF');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENST','VENST','VENTA MERCADO STOCK','VENTA','INST','S','S','N','N','S','VENTA','','N','COMST');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMST','COMST','COMPRA MERCADO STOCK','COMPRA','INST','S','S','N','N','S','COMPRA','','N','VENST');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMFUT','COMFUT','COMPRA FUTUROS','COMPRA','INFU','S','S','N','N','S','COMPRA','','N','VENFUT');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENFUT','VENFUT','VENTA FUTUROS','COMPRA','INFU','S','S','N','N','S','VENTA','','N','COMFUT');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMACN','COMACN','COMPRA ACCIONES NACIONALES','COMPRA','INAC','S','S','N','N','S','COMPRA','','N','VENACN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENACN','VENACN','VENTA ACCIONES NACIONALES','VENTA','INAC','S','S','N','S','S','VENTA','','N','COMACN');


--15-01-2015 Modificaciones Tabla Orden
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdBolsaDistribucion"	NVARCHAR(3) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"EspecificaBolsa"	NVARCHAR(1) NOT NULL DEFAULT 'S');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdTipoCanalDistribucion"	NVARCHAR(3) NOT NULL DEFAULT ''); 


DROP TABLE "BSCL.Orden.BD::ORD.EstadoOrd";

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.EstadoOrden" (
 "IdEstadoOrden" 							BIGINT NOT NULL,
 "IdEstado" 							NVARCHAR(3) NOT NULL,
 "NombreEntidad" 						NVARCHAR(100) NOT NULL, PRIMARY KEY ("IdEstadoOrden"))  ;
 
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (1,'ING','Transaccion');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (2,'ANU','Transaccion');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (3,'ASP','Transaccion.Asignacion');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (4,'ASC','Transaccion.Asignacion');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (5,'SAS','Transaccion.Asignacion');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (6,'ING','Orden');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (7,'CNF','Orden');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (8,'RZD','Orden');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (9,'OFE','Orden');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (10,'CAN','Orden');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (11,'ANU','Orden');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (12,'ASP','Orden.Asignacion');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (13,'ASC','Orden.Asignacion');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (14,'SAS','Orden.Asignacion');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (15,'NEF','Asignacion');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (16,'ANU','Asignacion');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (17,'EAF','Asignacion');
 
 
DROP TABLE "BSCL.Facturacion.BD::FAC.MovimientoFormaDePago"

create column table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" (
   "IdMovimientoFormaDePago"   BIGINT NOT NULL,
   "IdMovimientoComprobante"   BIGINT NOT NULL,
   "IdTipoMovimiento"          NVARCHAR(6) NOT NULL,   
   "IdMedioDePago"             NVARCHAR(3) NOT NULL,
   "NombreMedioDePago"         NVARCHAR(100) DEFAULT '',
   "NumeroCheque"              NVARCHAR(40) DEFAULT '0',
   "IdCuentaBancaria"          BIGINT DEFAULT 0,
   "NombreCuentaBancaria"      NVARCHAR(100) DEFAULT '',   
   "IdTipoCuentaBancaria"      NVARCHAR(4) DEFAULT '',
   "NombreTipoCuentaBancaria"  NVARCHAR(100) DEFAULT '',
   "NumeroCuentaBancaria"      BIGINT DEFAULT 0,
   "IdTarjetaBancaria"         BIGINT DEFAULT 0,   
   "IdTipoTarjetaBancaria"     NVARCHAR(4) DEFAULT '',
   "NombreTipoTarjetaBancaria" NVARCHAR(40) DEFAULT '',
   "NumeroTarjetaBancaria"     BIGINT DEFAULT 0,
   "IdModalidadDePago"         NVARCHAR(6) DEFAULT '', 
   "NombreModalidadDePago"     NVARCHAR(100) NOT NULL, 
   "IdBanco"                   BIGINT DEFAULT 0,
   "NombreBanco"               NVARCHAR(100) DEFAULT '',   
   "Sucursal"                  NVARCHAR(40) DEFAULT '',
   "FechaVctoDocumento"        DATE DEFAULT '0001-01-01',
   "Monto"                     DECIMAL(26,8) NOT NULL,
   "IdEstado"                  NVARCHAR(3) NOT NULL,
   "FechaIngresoRegistro"      TIMESTAMP NOT NULL,
 primary key ("IdMovimientoFormaDePago")
);

--15-01-2016 cambia nombre de servicio.... deja de ser "RV" para ser único, independiente del mercado
	update bscl."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Custodia.BD.Servicios::IngresoManualTitulos' where "IdServicio" = 3;
	
-- 15-01-2015: Se crea nueva estructura de tabla Log y detalle Log

DROP TABLE "BSCL.Plataforma.BD::PLA.DetalleLog"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.Log"  cascade; 

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Log" (
	 "IdLog" 					BIGINT CS_FIXED NOT NULL ,
	 "IdTipoLog" 				NVARCHAR(40),
	 "IdModulo"					NVARCHAR(3),
	 "IdUsuario"				BIGINT CS_FIXED,
	 "IdEmpresa"				BIGINT CS_FIXED,
	 "FechaLog"					LONGDATE CS_LONGDATE,
	 "PeriodoLog"				NVARCHAR(7),
	 "IdUsuarioAtendido"		BIGINT CS_FIXED,
	 "IdCliente"				BIGINT CS_FIXED,
	 "EstadoCliente"			NVARCHAR(3),
	 "NombreCliente"			NVARCHAR(100), 
	 "IdTipoDocumentoIdentidad"	NVARCHAR(40), 
	 "IdDocumentoIdentidad"		NVARCHAR(40), 
	 "IdPortafolio"				BIGINT CS_FIXED,
	 "EstadoPortafolio"			NVARCHAR(3),
	 "IdRegistro"				BIGINT CS_FIXED,
	 "EstadoRegistro"			NVARCHAR(3),
	 "IdTipoRegistro"			NVARCHAR(40), 
	 "IdTipoOperacion"			NVARCHAR(40), 
	 "Folio"					BIGINT CS_FIXED,
	 "Cantidad"					INTEGER CS_INT,
	 "PrecioTasa"				INTEGER CS_INT,
	 "Monto"					INTEGER CS_INT,
	 "Nemo"						NVARCHAR(260),
	 "IdProceso"				BIGINT CS_FIXED,
	 "EstadoProceso"			NVARCHAR(3),
	 "NombreProceso"			NVARCHAR(100),
	 PRIMARY KEY ("IdLog")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.DetalleLog" (
	 "IdDetalleLog"			BIGINT CS_FIXED NOT NULL, 
	 "IdLog"				BIGINT CS_FIXED NOT NULL, 
	 "SubRegistro"			NVARCHAR(40), 
	 "CampoRegistro"		NVARCHAR(40), 
	 "ValorAntiguo"			NVARCHAR(40), 
	 "ValorNuevo"			NVARCHAR(40), 
	 PRIMARY KEY ("IdDetalleLog")
);

ALTER TABLE "BSCL.Plataforma.BD::PLA.DetalleLog" 
ADD CONSTRAINT "FK_Log_DetalleLog" FOREIGN KEY ( "IdLog" ) 
REFERENCES "BSCL.Plataforma.BD::PLA.Log" ("IdLog") ON UPDATE RESTRICT ON DELETE RESTRICT;


-- 15-01-2015: Secuencia para la tabla DetalleLog
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetalleLog"	CASCADE;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetalleLog" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
       
Delete from "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion";
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(1,'MOVCAR','CTA.CargoAbonoMovimiento','ALL','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(2,'MOVABN','CTA.CargoAbonoMovimiento','ALL','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(3,'ABVTAC','CTA.CargoAbonoMovimiento','CTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(4,'ABAPCT','CTA.CargoAbonoMovimiento','CTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(5,'ABTRBC','CTA.CargoAbonoMovimiento','CTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(6,'ABDVAC','CTA.CargoAbonoMovimiento','CTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(7,'ABREGU','CTA.CargoAbonoMovimiento','CTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(8,'ABVCCU','CTA.CargoAbonoMovimiento','CTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(9,'CACPAC','CTA.CargoAbonoMovimiento','CTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(10,'CACPFM','CTA.CargoAbonoMovimiento','CTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(11,'CACPRF','CTA.CargoAbonoMovimiento','CTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(12,'CAREGU','CTA.CargoAbonoMovimiento','CTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(13,'CARTCT','CTA.CargoAbonoMovimiento','CTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(14,'ABTRAN','CTA.CargoAbonoMovimiento','CTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(15,'CATRAN','CTA.CargoAbonoMovimiento','CTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(16,'CATCAC','CTA.CargoAbonoMovimiento','FAC','INAC','COMACX','TOTAL');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(17,'CANCAC','CTA.CargoAbonoMovimiento','FAC','INAC','COMACX','NETO');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(18,'ABTVAC','CTA.CargoAbonoMovimiento','FAC','INAC','VENACX','TOTAL');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(19,'ABNVAC','CTA.CargoAbonoMovimiento','FAC','INAC','VENACX','NETO');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(20,'CACCAC','CTA.CargoAbonoMovimiento','FAC','INAC','COMACX','COBRO');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(21,'CACVAC','CTA.CargoAbonoMovimiento','FAC','INAC','VENACX','COBRO');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(22,'CATCAN','CTA.CargoAbonoMovimiento','FAC','INAC','COMACN','TOTAL');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(23,'CANCAN','CTA.CargoAbonoMovimiento','FAC','INAC','COMACN','NETO');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(24,'ABTVAN','CTA.CargoAbonoMovimiento','FAC','INAC','VENACN','TOTAL');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(25,'ABNVAN','CTA.CargoAbonoMovimiento','FAC','INAC','VENACN','NETO');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(26,'CACCAN','CTA.CargoAbonoMovimiento','FAC','INAC','COMACN','COBRO');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(27,'CACVAN','CTA.CargoAbonoMovimiento','FAC','INAC','VENACN','COBRO');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(28,'GAINSP','CTA.CargoAbonoMovimiento','GTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(29,'GAINRF','CTA.CargoAbonoMovimiento','GTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(30,'GAINFU','CTA.CargoAbonoMovimiento','GTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(31,'GAINSI','CTA.CargoAbonoMovimiento','GTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(32,'GAINVC','CTA.CargoAbonoMovimiento','GTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(33,'GAINFW','CTA.CargoAbonoMovimiento','GTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(34,'GARTRF','CTA.CargoAbonoMovimiento','GTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(35,'GARTFU','CTA.CargoAbonoMovimiento','GTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(36,'GARTSI','CTA.CargoAbonoMovimiento','GTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(37,'GARTVC','CTA.CargoAbonoMovimiento','GTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(38,'GARTFW','CTA.CargoAbonoMovimiento','GTA','','','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(48,'CATPRI','CTA.CargoAbonoMovimiento','FAC','INAC','PRESTA','TOTAL');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(47,'CATPRE','CTA.CargoAbonoMovimiento','FAC','INAC','EMPRES','TOTAL');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(41,'ANTSIA','CTA.CargoAbonoMovimiento','CUS','INSI','COMSIM','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(42,'ANTSIC','CTA.CargoAbonoMovimiento','CUS','INSI','VENSIM','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(43,'VCTSIA','CTA.CargoAbonoMovimiento','CUS','INSI','COMSIM','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(44,'VCTSIC','CTA.CargoAbonoMovimiento','CUS','INSI','VENSIM','');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(39,'CATCSI','CTA.CargoAbonoMovimiento','FAC','INSI','COMSIM','TOTAL');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(40,'CATVSI','CTA.CargoAbonoMovimiento','FAC','INSI','VENSIM','TOTAL');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(45,'CATCFU','CTA.CargoAbonoMovimiento','FAC','INFU','COMFUT','TOTAL');
insert into "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConceptoCuentaInversion", "IdConcepto", "NombreEntidad", "Origen", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia") values(46,'CATVFU','CTA.CargoAbonoMovimiento','FAC','INFU','VENFUT','TOTAL');       





---LIBERACION SOCIO DE NEGOCIO INICIO
SET SCHEMA "BSCL";
--15-01-2015 Modificaciones estructura Operador
DROP TABLE "BSCL.SocioNegocio.BD::OPE.Operador"; 
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::OPE.Operador" (
 "IdOperador"			    BIGINT  NOT NULL,
 "IdPersona" 				BIGINT,
 "CodigoOperador" 			NVARCHAR(40),
 "IdEstado" 				NVARCHAR(3),
 "CodigoTipoOperador" 		NVARCHAR(3),
 "IdSucursal" 				BIGINT,
 "FechaInicioCargo" 		DATE  DEFAULT '0001-01-01' NOT NULL ,
 "FechaTerminoCargo" 		DATE  DEFAULT '0001-01-01' NOT NULL ,
 "FechaIngreso" 			SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" 			SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion"		SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL , 
 "Borrado" 					NVARCHAR(1) DEFAULT 'N',
  PRIMARY KEY ("IdOperador"))  ;
  
INSERT INTO "BSCL.SocioNegocio.BD::OPE.Operador" ( "IdOperador","IdPersona","CodigoOperador","IdEstado","CodigoTipoOperador","IdSucursal")  
VALUES(1,169114,'30','ACT','INT',1);	
INSERT INTO "BSCL.SocioNegocio.BD::OPE.Operador" ( "IdOperador","IdPersona","CodigoOperador","IdEstado","CodigoTipoOperador","IdSucursal")  
VALUES(2,169115,'50','ACT','EXT',1);	

--15-01-2015 Modificaciones Tabla Contraparte
alter table "BSCL.SocioNegocio.BD::CNT.Contraparte" add (
 "IdEmpresa" BIGINT ,
 "EsEnrolado" 				NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "FechaIngreso" SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion" SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL
);

--15-01-2015 Modificaciones Tabla Empleado
alter table "BSCL.SocioNegocio.BD::EMP.Empleado" add (
 "EsEnrolado" 				NVARCHAR(1) DEFAULT 'N' NOT NULL 
);

--15-01-2015 Modificaciones estructura ConsultarEnrolamiento
-- Tipos
drop type "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento";
CREATE TYPE "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento" AS TABLE (
"IdEnrolamiento"			BIGINT,
"IdEmpresa" 				BIGINT,
"IdUsuario" 				BIGINT,
"IdTipoEntidad" 			NVARCHAR(100),
"IdEntidadEnrolada" 		BIGINT,
"IdTipoDocumentoIdentidad"  NVARCHAR(1),
"DocumentoIdentidad" 		NVARCHAR(40),
"Tipo" 						NVARCHAR(2),
"Nombres" 					NVARCHAR(100),
"ApellidoPaterno" 			NVARCHAR(100),
"ApellidoMaterno" 			NVARCHAR(100),
"RazonSocial" 				NVARCHAR(100),
"IdPaisNacimiento" 			BIGINT,
"FechaNacimiento" 			DATE,
"Sexo" 						NVARCHAR(2),
"EstadoCivil" 				NVARCHAR(2),
"IdCiudad" 					BIGINT,
"Telefono" 					NVARCHAR(100),
"Email" 					NVARCHAR(100),
"Direccion" 				NVARCHAR(100),
"FechaCreacion" 			SECONDDATE,
"FechaModificacion" 		SECONDDATE,
"CLI_IdCategoriaInversionista" NVARCHAR(40),
"POR_IdPerfilComercial" 	NVARCHAR(40), 
"EMP_CodigoEmpleado"			NVARCHAR(40),
"EMP_CodigoTipoCargoEmpleado"	NVARCHAR(3),
"EMP_IdEmpleadoSupervisor"		BIGINT,
"EMP_IdSucursal"				BIGINT,
"EMP_FechaInicioCargo"			DATE,
"EMP_FechaTerminoCargo" 		DATE,
"CNT_Codigo" 					NVARCHAR(40),
"CNT_Emisor" 					NVARCHAR(1),
"CNT_Depositos"					NVARCHAR(1),
"CNT_Bolsa" 					NVARCHAR(1),
"CNT_Corredora" 				NVARCHAR(1),
"CNT_CamaraDeCompensacion" 		NVARCHAR(1),
"CNT_Banco" 					NVARCHAR(1)  
 );

--15-01-2015 Modificaciones Tabla Enrolamiento
alter table  "BSCL.SocioNegocio.BD::SOC.Enrolamiento" add (
   "EMP_CodigoEmpleado"				NVARCHAR(40) 	DEFAULT '',
   "EMP_CodigoTipoCargoEmpleado"	NVARCHAR(3)	 	DEFAULT '',
   "EMP_IdEmpleadoSupervisor"		BIGINT 			DEFAULT 0,
   "EMP_IdSucursal"					BIGINT			DEFAULT 0,
   "EMP_FechaInicioCargo"			DATE  			DEFAULT '0001-01-01',
   "EMP_FechaTerminoCargo" 			DATE			DEFAULT '0001-01-01',
   "CNT_Codigo" 					NVARCHAR(40)	DEFAULT '',
   "CNT_Emisor" 					NVARCHAR(1)		 DEFAULT 'N',
   "CNT_Depositos"					NVARCHAR(1)		 DEFAULT 'N',
   "CNT_Bolsa" 						NVARCHAR(1)		 DEFAULT 'N',
   "CNT_Corredora" 					NVARCHAR(1)		 DEFAULT 'N',
   "CNT_CamaraDeCompensacion" 		NVARCHAR(1)		 DEFAULT 'N',
   "CNT_Banco" 						NVARCHAR(1)		 DEFAULT 'N'  
);

---LIBERACION SOCIO DE NEGOCIO FIN

--15-01-2015 Modificaciones Tabla emisor
alter table  "BSCL.Mercado.BD::MER.Emisor" add (
 "EmiteIIF"  							NVARCHAR(1)			not null default 'N',
 "EmiteIRF"  							NVARCHAR(1)			not null default 'N',
 "EmiteFungible"  						NVARCHAR(1)			not null default 'N',
 "AutorizaPactos"  						NVARCHAR(1)			not null default 'N',
 "ClasificacionRiesgoIIF"				NVARCHAR(40) 		not null default ''
);


--15-01-2015 Modificaciones tabla usuario
RENAME COLUMN "BSCL"."BSCL.Plataforma.BD::ACC.Usuario"."Nombre" TO "CodigoUsuario";
alter table "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" ADD ("TipoEntidad" NVARCHAR(40), "IdEntidad" BIGINT);
update "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" set "TipoEntidad" = 'EMP', "IdEntidad" = 163 WHERE "CodigoUsuario" = 'MASTER';

--15-01-2015 
alter table "BSCL"."BSCL.Custodia.BD::CUS.ConceptoCustodia" 
add (
       "IdClasificacionTipoInstrumento" NVARCHAR(3) not null default '',
       "IdTipoOperacion"           NVARCHAR(6)   not null default '',
       "ConCertificadoAutomatico"  NVARCHAR(1)   not null default 'N'
       );

--15-01-2015      
alter table "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion"  add ("AccionCustodia" NVARCHAR(40) DEFAULT '');

delete from "BSCL.Plataforma.BD::INS.TipoOperacion";
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMACX','COMACX','COMPRA ACCIONES EXTRANJERAS','COMPRA','INAX','S','S','N','N','S','COMPRA','COMPRA','N','VENACX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENACX','VENACX','VENTA ACCIONES EXTRANJERAS','VENTA','INAX','S','S','N','N','S','VENTA','VENTA','N','COMACX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMSIM','COMSIM','COMPRA DE SIMULTÁNEA','COMPRA','INSI','S','S','N','N','S','COMPRA','COMPRA','N','VENSIM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENSIM','VENSIM','VENTA DE SIMULTÁNEA','VENTA','INSI','S','S','N','N','S','VENTA','VENTA','N','COMSIM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMCFI','COMCFI','COMPRA CFI NACIONAL','COMPRA','INCI','S','S','N','N','S','COMPRA','COMPRA','N','VENCFI');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENCFI','VENCFI','VENTA CFI NACIONAL','VENTA','INCI','S','S','N','N','S','VENTA','VENTA','N','COMCFI');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMCFX','COMCFX','COMPRA CFI EXTRANJERO','COMPRA','INCX','S','S','N','N','S','COMPRA','COMPRA','N','VENCFX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENCFX','VENCFX','VENTA CFI EXTRANJERO','VENTA','INCX','S','S','N','N','S','VENTA','VENTA','N','COMCFX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMMON','COMMON','COMPRA MONETARIO','COMPRA','INMO','S','S','N','N','S','COMPRA','COMPRA','N','VENMON');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENMON','VENMON','VENTA MONETARIO','VENTA','INMO','S','S','N','N','S','VENTA','VENTA','N','COMMON');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMETN','COMETN','COMPRA ETF NACIONAL','COMPRA','INEN','S','S','N','N','S','COMPRA','COMPRA','N','VENETN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENETN','VENETN','VENTA ETF NACIONAL','VENTA','INEN','S','S','N','N','S','VENTA','VENTA','N','COMETN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMETX','COMETX','COMPRA ETF EXTRANJERO','COMPRA','INEX','S','S','N','N','S','COMPRA','COMPRA','N','VENETX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENETX','VENETX','VENTA ETF EXTRANJERO','VENTA','INEX','S','S','N','N','S','VENTA','VENTA','N','COMETX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMCFM','COMCFM','COMPRA CFM','COMPRA','INCM','S','S','N','N','S','COMPRA','COMPRA','N','VENCFM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENCFM','VENCFM','VENTA CFM','VENTA','INCM','S','S','N','N','S','VENTA','VENTA','N','COMCFM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMPAC','COMPAC','COMPRA CON PACTO','COMPRA','MERF','S','N','N','N','N','COMPRA','COMPRA','N','VENPAC');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENPAC','VENPAC','VENTA CON PACTO','VENTA','MERF','S','N','N','N','N','VENTA','VENTA','N','COMPAC');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMTER','COMTER','COMPRA A TERMINO','COMPRA','MERF','S','N','N','N','N','COMPRA','COMPRA','N','VENTER');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENTER','VENTER','VENTA A TERMINO','VENTA','MERF','S','N','N','N','N','VENTA','VENTA','N','COMTER');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('INGTIT','INGTIT','INGRESO DE TITULO','INGRESO','CUST','N','N','N','N','N','INGRESO','','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('EGRTIT','EGRTIT','EGRESO DE TITULO','EGRESO','CUST','N','N','N','N','N','EGRESO','','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('TRATIT','TRATIT','TRASPASO DE TITULOS','TRASPASO','CUST','N','N','N','N','N','TRASPASO','','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('PRESTA','PRESTA','PRESTAMO','COMPRA','INAC','N','N','N','N','N','COMPRA','VENTA','N','EMPRES');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('EMPRES','EMPRES','EMPRESTITO','VENTA','INAC','N','N','N','N','N','VENTA','COMPRA','N','PRESTA');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMFWD','COMFWD','COMPRA DE FORWARD','COMPRA','MEFW','S','N','N','N','N','COMPRA','COMPRA','N','VENFWD');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENFWD','VENFWD','VENTA DE FORWARD','VENTA','MEFW','S','N','N','N','N','VENTA','VENTA','N','COMFWD');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMSPT','COMSPT','COMPRA SPOT','COMPRA','MEME','S','N','N','N','N','COMPRA','COMPRA','N','VENSPT');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENSPT','VENSPT','VENTA SPOT','VENTA','MEME','S','N','N','N','N','VENTA','VENTA','N','COMSPT');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('APORFM','APORFM','APORTE FONDO MUTUO','APORTE','INFM','N','N','N','N','N','APORTE','','N','RESCFM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('RESCFM','RESCFM','RESCATE FONDO MUTUO','RESCATE','INFM','N','N','N','N','N','RESCATE','','N','APORFM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('CARMAN','CARMAN','CARGO MANUAL','CARGO','CTIN','N','N','N','N','N','CARGO','','N','ABNMAN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('ABNMAN','ABNMAN','ABONO MANUAL','ABONO','CTIN','N','N','N','N','N','ABONO','','N','CARMAN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('CARTRA','CARTRA','CARGO TRASPASO','CARGO','CTIN','N','N','N','N','N','CARGO','','N','ABNTRA');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMIIF','COMIIF','COMPRA INTERMEDIACION FINANCIERA','COMPRA','INIF','S','S','N','N','S','COMPRA','COMPRA','N','VENIIF');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENIIF','VENIIF','VENTA INTERMEDIACION FINANCIERA','VENTA','INIF','S','S','N','N','S','VENTA','VENTA','N','COMIIF');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMRF','COMRF','COMPRA MERCADO RENTA FIJA','COMPRA','INRF','S','S','N','N','S','COMPRA','COMPRA','N','VENRF');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENRF','VENRF','VENTA MERCADO RENTA FIJA','VENTA','INRF','S','S','N','N','S','VENTA','VENTA','N','COMRF');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENST','VENST','VENTA MERCADO STOCK','VENTA','INST','S','S','N','N','S','VENTA','VENTA','N','COMST');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMST','COMST','COMPRA MERCADO STOCK','COMPRA','INST','S','S','N','N','S','COMPRA','COMPRA','N','VENST');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMFUT','COMFUT','COMPRA FUTUROS','COMPRA','INFU','S','S','N','N','S','COMPRA','COMPRA','N','VENFUT');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENFUT','VENFUT','VENTA FUTUROS','COMPRA','INFU','S','S','N','N','S','VENTA','COMPRA','N','COMFUT');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMACN','COMACN','COMPRA ACCIONES NACIONALES','COMPRA','INAC','S','S','N','N','S','COMPRA','COMPRA','N','VENACN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENACN','VENACN','VENTA ACCIONES NACIONALES','VENTA','INAC','S','S','N','S','S','VENTA','VENTA','N','COMACN');
        
