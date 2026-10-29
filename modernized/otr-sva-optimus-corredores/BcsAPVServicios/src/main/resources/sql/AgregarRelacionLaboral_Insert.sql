INSERT INTO "BSCL.SocioNegocio.BD::PER.RelacionLaboral"
( "IdPersona"				--BIGINT			NOT NULL
, "IdEmpleador"				--BIGINT			NOT NULL
, "CodigoTipoCargoEmpleado"	--NVARCHAR(6)			NOT NULL
, "IngresoMensual"			--DECIMAL(26,8)	DEFAULT 0 NOT NULL
, "FechaIngresoCargo"       --DATE  DEFAULT '0001-01-01' NOT NULL
, "FechaRetiroCargo"		--DATE  DEFAULT '0001-01-01' NOT NULL
, "IdEstado"				--NVARCHAR(3)		NOT NULL
)
values (
  :IdPersona
, :IdEmpleador
, :CodigoTipoCargoEmpleado
, :IngresoMensual
, :FechaIngresoCargo
, :FechaRetiroCargo
, :IdEstado
);