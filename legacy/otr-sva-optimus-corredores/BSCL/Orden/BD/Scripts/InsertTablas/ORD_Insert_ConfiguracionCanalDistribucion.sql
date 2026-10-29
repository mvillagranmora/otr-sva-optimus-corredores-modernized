SET SCHEMA "BSCL"; 

--REPORTES
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','AGE','RRV','BVL','N','N',0 FROM DUMMY;
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','AGE','RRF','BVL','N','N',0 FROM DUMMY;
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','RRV','BVL','N','N',0 FROM DUMMY;
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','RRF','BVL','N','N',0 FROM DUMMY;
		

--ACN
	--BCS
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','TRA','ACN','BCS','N','N',0 FROM DUMMY;
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','AGE','ACN','BCS','N','N',0 FROM DUMMY;
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'RUA','AGE','ACN','BCS','N','N',0 FROM DUMMY;
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'RUR','AGE','ACN','BCS','N','N',0 FROM DUMMY;

	--BEC
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','AGE','ACN','BEC','N','N',0 FROM DUMMY;
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','TRA','ACN','BEC','N','N',0 FROM DUMMY;

--CFI
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','AGE','CFI','BCS','N','N',0 FROM DUMMY;
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'RUA','AGE','CFI','BCS','N','N',0 FROM DUMMY;
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','CFI','BCS','N','N',0 FROM DUMMY;

--CFM
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','AGE','CFM','BCS','N','N',0 FROM DUMMY;
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'RUA','AGE','CFM','BCS','N','N',0 FROM DUMMY;
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','CFM','BCS','N','N',0 FROM DUMMY;

--CFX
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','AGE','CFX','BCS','N','N',0 FROM DUMMY;
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'RUA','AGE','CFX','BCS','N','N',0 FROM DUMMY;
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','CFX','BCS','N','N',0 FROM DUMMY;

--MIL
	--BCS
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','AGE','MIL','BCS','N','N',0 FROM DUMMY;
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','TRA','MIL','BCS','N','N',0 FROM DUMMY;

	--BVL
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','AGE','MIL','BVL','N','N',0 FROM DUMMY;
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','TRA','MIL','BVL','N','N',0 FROM DUMMY;
	
	--BVC
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','AGE','MIL','BVC','N','N',0 FROM DUMMY;
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','TRA','MIL','BVC','N','N',0 FROM DUMMY;

	--BMV
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','AGE','MIL','BMV','N','N',0 FROM DUMMY;
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','TRA','MIL','BMV','N','N',0 FROM DUMMY;
	
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','AGE','MIL','0','N','N',0 FROM DUMMY;
	

--ETN
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','AGE','ETN','BCS','N','N',0 FROM DUMMY;
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','ETN','BCS','N','N',0 FROM DUMMY;

--ETX
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','AGE','ETX','BCS','N','N',0 FROM DUMMY;
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','TRA','ETX','BCS','N','N',0 FROM DUMMY;

--MON
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','AGE','MON','BCS','N','N',0 FROM DUMMY;
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	SELECT 1,'MAN','TRA','MON','BCS','N','N',0 FROM DUMMY;


	
--SIM
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','SIM','BCS','N','N',0 FROM DUMMY;
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','AGE','SIM','BCS','N','N',0 FROM DUMMY;
--ACX
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','ACX','BCS','N','N',0 FROM DUMMY;
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','AGE','ACX','BCS','N','N',0 FROM DUMMY;



--PAG
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','PAG','BCS','N','N',0 FROM DUMMY;

--ZER
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','ZER','BCS','N','N',0 FROM DUMMY;

--CER
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','CER','BCS','N','N',0 FROM DUMMY;

--PRC
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','PRC','BCS','N','N',0 FROM DUMMY;

--LTH
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','LTH','BCS','N','N',0 FROM DUMMY;

--BEM
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','BEM','BCS','N','N',0 FROM DUMMY;

insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','BBC','BCS','N','N',0 FROM DUMMY;

--BCP
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','BCP','BCS','N','N',0 FROM DUMMY;

--BCU
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','BCU','BCS','N','N',0 FROM DUMMY;

--BTP
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','BTP','BCS','N','N',0 FROM DUMMY;

--BTU
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','BTU','BCS','N','N',0 FROM DUMMY;


--CONFIGURACION PERU
insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','TRA','ACN','BVL','N','N',0 FROM DUMMY;

insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
SELECT 1,'MAN','AGE','ACN','BVL','N','N',0 FROM DUMMY;




rollback;

commit;
