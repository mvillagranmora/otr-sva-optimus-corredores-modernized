/*****************************************************************************************************************
.______    __          ___   .___________.    ___       _______   ______   .______      .___  ___.      ___      
|   _  \  |  |        /   \  |           |   /   \     |   ____| /  __  \  |   _  \     |   \/   |     /   \     
|  |_)  | |  |       /  ^  \ `---|  |----`  /  ^  \    |  |__   |  |  |  | |  |_)  |    |  \  /  |    /  ^  \    
|   ___/  |  |      /  /_\  \    |  |      /  /_\  \   |   __|  |  |  |  | |      /     |  |\/|  |   /  /_\  \   
|  |      |  `----./  _____  \   |  |     /  _____  \  |  |     |  `--'  | |  |\  \----.|  |  |  |  /  _____  \  
| _|      |_______/__/     \__\  |__|    /__/     \__\ |__|      \______/  | _| `._____||__|  |__| /__/     \__\ 
                                                                                                                 
******************************************************************************************************************/
-- INDICADORES
set schema bscl;

	--SELECT *  FROM "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" in (1011,1012,1013);
	--SELECT *  FROM "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" in (1002,1003,1004,1005,1006,1007,1008,1009,1010);
	--SELECT *  FROM "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" in (1014,1015,1016,1017,1018,1019,1020);
	--delete FROM "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" in (1002,1003,1004,1005,1006,1007,1008,1009,1010);
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
	VALUES (10002, 'CLIENTE', 1, 'N', 'CHECKBOX', 'FICHA DEL CLIENTE COMPLETA','DOC_FCCOMP', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
	VALUES (10003, 'CLIENTE', 1, 'N', 'CHECKBOX', 'FORMULARIO DECLARACIÓN ORIGEN DE FONDOS','DOC_FDEDOF', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
	VALUES (10004, 'CLIENTE', 1, 'N', 'CHECKBOX', 'FORMULARIO FATCA (SI ES US-PERSON)','DOC_FFATCA', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
	VALUES (10005, 'CLIENTE', 1, 'N', 'CHECKBOX', 'FORMULARIO CONTRATO DE MEDIOS (INTERNET)','DOC_FCNMED', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
	VALUES (10006, 'CLIENTE', 1, 'N', 'CHECKBOX', 'FORMULARIO NEGOCIACION MVE','DOC_FNEMVE', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
	VALUES (10007, 'CLIENTE', 1, 'N', 'CHECKBOX', 'CONTRATO NEGOCIACION MVE','DOC_CNNMVE', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
	VALUES (10008, 'CLIENTE', 1, 'N', 'CHECKBOX', 'CONTRATO NEGOCIACION MILA','DOC_CNMILA', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
	VALUES (10009, 'CLIENTE', 1, 'N', 'CHECKBOX', 'CONTRATO NEGOCIACION DE VALORES EN EL SEGMENTO RIESGO','DOC_CNVSRI', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
	VALUES (10010, 'CLIENTE', 1, 'N', 'CHECKBOX', 'CONTRATO NEGOCIACION DE MVE AGENTE PROMOTOR','DOC_CNMVEA', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
	VALUES (10011, 'CLIENTE', 1, 'N', 'CHECKBOX', 'CONTRATO NEGOCIACION MERCADO ALTERNATIVO NAV','DOC_CNMANA', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
	VALUES (10012, 'CLIENTE', 1, 'N', 'CHECKBOX', 'NEGOCIACIÓN DE BONOS ACREDITADOS','DOC_NEGOBA', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );
	
	--ORDENES
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
	VALUES (10013, 'PORTAFOLIO', 1, 'N', 'NVARCHAR', 'MODALIDAD DE ÓRDENES','ORDMODORDN', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 0, 'N' );
	
	-- INDICADORES DE PERSONA; VALIDAR CONDICIONALIDAD QUE MENCIONAN EN RQ
	set schema bscl;
	--delete from "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" in (1014,1015,1016,1017,1018,1019,1020);
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
	VALUES (10014, 'PERSONA', 1, '', 'COMBO', 'Nivel de Experiencia en MV','GENNIVEXPE', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','A' );
		--DETALLE!
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (1,10014,'NINGUNO','Ninguno');
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (2,10014,'POCO','Poco');
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (3,10014,'REGULAR','Regular');
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (4,10014,'SICONOCE','Sí conoce');
		
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
	VALUES (10015, 'PERSONA', 1, '', 'COMBO', 'Primera Relación con CCB','GENPRIMREL', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','A' );
		--DETALLE!
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (1,10015,'VINCULADO','Vinculado o relacionado al AI');
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (2,10015,'NOVINCULADO','No vinculado al AI');
	
	select * FROM "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" = 10016;
	
	DELETE FROM "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" = 10016;
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
	VALUES (10016, 'PERSONA', 1, '', 'COMBO', 'Tipo de Primera Relación con CCB','GENTIPPRIR', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','NM' );
		--DETALLE!
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (1,10016,'01','Accionista, Director o Gerente del AI o de empresas de su Grupo Económico');
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (2,10016,'02','Representante del AI');
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (3,10016,'03','Funcionario de Control Interno, funcionario o personal del AI o de alguna empresa de su Grupo Económico');
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (4,10016,'04','Pariente de Accionista, Director, Gerente, Representante, Funcionario de Control Interno o de algún funcionario o personal del AI');
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (5,10016,'05','Otro tipo de vinculación o relación con el AI o con algún Accionista, Director, Gerente, Representante, Funcionario de Control Interno o Personal del AI');
		/*INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (6,10016,'06','');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (7,10016,'07','');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (8,10016,'08','');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (9,10016,'09','');*/
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
	VALUES (10026, 'PERSONA', 1, '', 'COMBO', 'Tipo de Primera Relación con CCB','GENTIPPRIRJUR', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','J' );
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (10,10026,'10','Conformante del mismo grupo económico');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (11,10026,'11','Otro tipo de vinculación');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (12,10026,'12','Cuenta propia');

	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
	VALUES (10017, 'PERSONA', 1, '', 'COMBO', 'Segunda Relación con CCB','GENSEGUREL', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','A' );
		--DETALLE!
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (1,10017,'CONCONTRATOADMIN','Con Contrato de Administración');
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (2,10017,'SINCONTRATOADMIN','Sin Contrato de Administración');
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
	VALUES (10018, 'PERSONA', 1, '', 'COMBO', 'Identificador de protección de datos','GENPROTDAT', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','A' );
		--DETALLE!
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (1,10018,'SI','Si');
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (2,10018,'NO','No');
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (3,10018,'CAN','CAN');
		INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (4,10018,'VAC','VAC');
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
	VALUES (10019, 'PERSONA', 1, '', 'NVARCHAR', 'Cargo Público','GENCARGPUB', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','N' );
	
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
	VALUES (10020, 'PERSONA', 1, '', 'NVARCHAR', 'Tipo Entidad SMV','GENTIENSMV', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','J' );  -- agregar tipo JM (juridica mancomunada) y combinaciones

	--PEP FALTANTE:
	set schema bscl;
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador"("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion","CodigoTipoIndicador","IdTipoPersona","RequiereFechaVencimiento","IdCarga","IdIndicadorMigracion")
	VALUES(10027,'PERSONA','1','N','CHECKBOX','FUNCION PÚBLICA','PEP','0','0','0','IND','ACT','N','REL_FPU','NJ','N','0','0');
	commit;
	--select * from "BSCL.Plataforma.BD::IND.TipoIndicador" where "CodigoTipoIndicador"='PEP';
	--select * from "BSCL.Plataforma.BD::IND.TipoIndicador" where "GrupoIndicador"='PEP';
	
	--AGREGAR A UAT y MB:
	set schema bscl;
	insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
		"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
		"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
	VALUES (1020, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Envío de Cartola a Dirección Física','CAR_ENVCDF', 'GRUPOCARTOLA', 'PRE', 'ACT', 0, 0, 0, 'N' );
	commit;
	
commit;


set schema bscl;
insert into "BSCL.Plataforma.BD::IND.TipoIndicador"
("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion","CodigoTipoIndicador","IdTipoPersona","RequiereFechaVencimiento","IdCarga","IdIndicadorMigracion")
VALUES(10027,'PERSONA','1','N','CHECKBOX','FUNCION PÚBLICA','PEP','0','0','0','IND','ACT','N','REL_PEP','NJ','N','0','0');
commit;

set schema bscl;
update "BSCL.Plataforma.BD::IND.TipoIndicador" set "CodigoTipoIndicador" = 'REL_FPU'
where "NombreTipoIndicador" = 'FUNCION PÚBLICA';
commit;