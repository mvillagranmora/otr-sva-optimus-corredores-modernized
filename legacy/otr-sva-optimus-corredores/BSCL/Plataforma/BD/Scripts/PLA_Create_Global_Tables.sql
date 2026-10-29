SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE "BSCL.Plataforma.BD::PLA.SaldoFlujoCaja_DatosFlujoCaja";


/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Plataforma.BD::PLA.SaldoFlujoCaja_DatosFlujoCaja"(
	 "FechaLiquidacion"			NVARCHAR(10)
	,"IdPortafolio"				BIGINT
	,"IdMoneda"					NVARCHAR(3)
	,"Monto"					DECIMAL(26,8)
	,"MontoCompra"				DECIMAL(26,8)
	,"MontoVenta"				DECIMAL(26,8)
	,"ItemCalculo"				NVARCHAR(40)
) ON COMMIT DELETE ROWS;


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
SELECT * FROM "BSCL.Plataforma.BD::PLA.SaldoFlujoCaja_DatosFlujoCaja";
