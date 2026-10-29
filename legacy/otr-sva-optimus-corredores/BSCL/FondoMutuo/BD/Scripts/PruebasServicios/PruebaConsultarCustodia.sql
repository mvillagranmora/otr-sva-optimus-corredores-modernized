SELECT * FROM "BSCL"."BSCL.FondoMutuo.BD::FMM.SerieDeFondo";
SELECT * FROM "BSCL"."BSCL.FondoMutuo.BD::FMM.Fondo";
SELECT * FROM "BSCL"."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::INS.TipoProducto";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento";
SELECT * FROM "BSCL"."BSCL"."BSCL.Plataforma.BD::PLA.Boveda";
SELECT * FROM "BSCL"."BSCL.Custodia.BD::CUS.ConceptoCustodia";
SELECT "IdInstrumento", * FROM "BSCL"."BSCL.Custodia.BD::CUS.Cartera" WHERE "IdCartera" = 20575413;
SET SCHEMA "BSCL";
DROP table #carteras;
DROP table #instrumentos;
DROP table #fechas;
DROP table #ejecucion;
create local temporary table #carteras(	"FolioCartera"						BIGINT, --TAMBIEN FOLIO CARTERA RELACIONADA EN OTRAS TABLAS
	"FolioGarantia"						BIGINT,
	"FolioSubyacente"					BIGINT,
	"IdCliente"							BIGINT,
	"IdPortafolio"						BIGINT);
create local temporary table #instrumentos(		
	"IdClasificacionTipoInstrumento"	NVARCHAR(3),
	"IdTipoInstrumento"					NVARCHAR(3),
	"IdInstrumento"						BIGINT,
	"IdEmisor"							NVARCHAR(40),
	"FechaVencimiento"					DATE,
	"IdTipoReajuste"					NVARCHAR(3),
	"EsFungible"						NVARCHAR(1),
	"IdBoveda"							NVARCHAR(3)
);
create local temporary table #fechas(
	"Fecha" DATE
);
create local temporary table #ejecucion(
	"Ejecutar" NVARCHAR(40),
   	"Todos" NVARCHAR(1),
   	"SoloSubyacentes" NVARCHAR(1),
   	"SoloGarantizables" NVARCHAR(1)
);		

insert into #carteras 
SELECT null as "FolioCartera",null as "FolioGarantia", null as "FolioSubyacente", null AS "IdCliente", 512348 AS "IdPortafolio" FROM DUMMY WHERE 1=1;

insert into #instrumentos
SELECT null as "IdClasificacionTipoInstrumento",null as "IdTipoInstrumento",346394 AS "IdInstrumento", null as "IdEmisor", null as "FechaVencimiento",
										null as "IdTipoReajuste", null as "EsFungible",'COR' as "IdBoveda" FROM DUMMY WHERE 1=1;
insert into #fechas
SELECT '2016-07-25' FROM DUMMY WHERE 1=1;

insert into #ejecucion
SELECT 'SaldoTransito' AS "Ejecutar",'N' AS "Todos",'N' AS "SoloSubyacentes",'N' AS "SoloGarantizables" FROM DUMMY WHERE 'N' = 'S' UNION ALL
SELECT 'SaldoCustodia' AS "Ejecutar",'S' AS "Todos",'N' AS "SoloSubyacentes",'N' AS "SoloGarantizables" FROM DUMMY WHERE 'S' = 'S' UNION ALL
SELECT 'SaldoCustodia' AS "Ejecutar",'N' AS "Todos",'N' AS "SoloSubyacentes",'S' AS "SoloGarantizables" FROM DUMMY WHERE 'N' = 'S' UNION ALL
SELECT 'SaldoCustodia' AS "Ejecutar",'N' AS "Todos",'S' AS "SoloSubyacentes",'N' AS "SoloGarantizables" FROM DUMMY WHERE 'N' = 'S' UNION ALL
SELECT 'SaldoGarantia' AS "Ejecutar",'N' AS "Todos",'N' AS "SoloSubyacentes",'N' AS "SoloGarantizables" FROM DUMMY WHERE 'N' = 'S' UNION ALL
SELECT 'SaldoSubyacente' AS "Ejecutar",'N' AS "Todos",'N' AS "SoloSubyacentes",'N' AS "SoloGarantizables" FROM DUMMY WHERE 'N' = 'S' UNION ALL
SELECT 'SaldoSubyacente' AS "Ejecutar",'N' AS "Todos",'N' AS "SoloSubyacentes",'N' AS "SoloGarantizables" FROM DUMMY WHERE 'N' = 'S' UNION ALL
SELECT 'SaldoSubyacente' AS "Ejecutar",'N' AS "Todos",'N' AS "SoloSubyacentes",'N' AS "SoloGarantizables"  FROM DUMMY WHERE 'N' = 'S';
 
CALL "BSCL.Custodia.BD.Servicios::ConsultarFlujoCustodia" ( 
				-- Parametros entrada comunes en todos los servicios publicos
			    p_IdEmpresa                     => 1,
			    p_Uso							=> 'CUSTODIA',
			    p_IdPais						=> 44,
				p_Instrumentos  				=> #instrumentos,
			    p_Carteras						=> #carteras ,
			    p_FechaSaldo					=> '2016-07-25',
			    p_DiasProyeccion 				=> 0,
			    p_EsAgrupadoPorFolioCartera 	=> 'S',
			    p_out_FlujoDetallado			=> ?,
			    p_out_FlujoTotalizado			=> ?
			    
			);
			
CALL "BSCL.Custodia.BD.Servicios::CalcularDisponible"(
			p_IdEmpresa			=> 1,
			p_Instrumentos		=> #instrumentos,
			p_Carteras			=> #carteras,
			p_IdOrigenGarantia	=> null,
			p_Fechas			=> #fechas,
			p_Ejecutar			=> #ejecucion,
			p_out_saldo			=> ?);