SET SCHEMA "BSCL";
--- Recibo tabla
DROP TABLE #Entrada;
CREATE LOCAL TEMPORARY TABLE #Entrada ("IdIndicador" NVARCHAR(10), "Fecha" DATE, "Valor" Decimal(18,4), "FechaReal" DATE);
insert into #Entrada values ('UF','1900-01-01', 0, '2016-01-01' );
insert into #Entrada values ('IVP','2016-01-01', 0, '2016-01-01' );
insert into #Entrada values ('ICP','2016-01-01', 0, '2016-01-01' );
insert into #Entrada values ('USD','2016-01-01', 0, '2016-01-01' );
insert into #Entrada values ('USD_ACU','2016-01-01', 0, '2016-01-01' );
insert into #Entrada values ('LIBOR','2016-01-01', 0, '2016-01-01' );
insert into #Entrada values ('TIP','2016-01-01', 0, '2016-01-01' );
insert into #Entrada values ('EUR','2016-01-01', 0, '2016-01-01' );
insert into #Entrada values ('YEN','2016-01-01', 0, '2016-01-01' );
call "BSCL.Plataforma.BD.Servicios::ObtenerValorIndicador"('2016-01-01', 'N', '','',#Entrada,?,?,?,?,?) ;
call "BSCL.Plataforma.BD.Servicios::ObtenerValorIndicador"('2016-01-01', 'N', 'USD','ACUERDO',#Entrada,?,?,?,?,?);
call "BSCL.Plataforma.BD.Servicios::ObtenerValorIndicador"('2016-01-01', 'N', 'USD','',#Entrada,?,?,?,?,?);

/*
	IN p_Fecha			DATE	     , 
	IN p_IndLocal		NVARCHAR(1)  ,            -- EJ: si es = N, entonces toma los valores directo de la RUEBAS
	IN p_Indicador		NVARCHAR(06) default '' , -- EJ: 'UF', 'USD', 'CLP'
	IN p_TipoIndicador  NVARCHAR(03) default '' , -- EJ: Tipos de Dolar 'OBSERVADO' (por defecto), 'ACUERDO'
	IN p_in_Indicadores table ("IdIndicador" NVARCHAR(10), "Fecha" DATE, "Valor" DECIMAL(26,8), "FechaReal" DATE),
									
	--Salida
    OUT p_out_Indicador NVARCHAR(06) ,
    OUT p_out_resultado Decimal(18,4),
   	OUT p_out_Fecha		DATE,
   	OUT P_OUT_ERROR		NVARCHAR(500),
   	OUT p_out_Indicadores table ("IdIndicador" NVARCHAR(10), "Fecha" DATE, "Valor" DECIMAL(26,8), "FechaReal" DATE)
*/
 	
/*
SELECT * FROM  "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio"
DELETE FROM  "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" WHERE "IdParidadYTipoCambio" = 121

INSERT INTO "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" ("IdParidadYTipoCambio" ,"Fecha","IdMoneda","IdUsuarioIngreso","TipoCambioMercado","ParidadMercado","IdEstado") 
VALUES (121,'2015-01-01','YEN',1,470,1.5,'ACT');
*/
