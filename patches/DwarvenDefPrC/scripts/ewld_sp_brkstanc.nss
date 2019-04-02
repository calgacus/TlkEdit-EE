#include "patchdefinitions"

void main()
{
	/*
	remove effects applied by ewld_sp_defstanc.nss
	*/
	
	effect e = GetFirstEffect( OBJECT_SELF );
	GetEffectSpellId( e );
	while ( GetIsEffectValid( e ) ){
		if ( GetEffectSpellId( e ) == DEFENSIVE_STANCE ){
			RemoveEffect( OBJECT_SELF, e );
			//break;
		}
		e = GetNextEffect( OBJECT_SELF );
	}
	
	// character remains winded for 3 rounds
	effect eWINDED= ExtraordinaryEffect( EffectAbilityDecrease( ABILITY_STRENGTH,     2 ) );
	ApplyEffectToObject(
		DURATION_TYPE_TEMPORARY,
		eWINDED,
		OBJECT_SELF,
		RoundsToSeconds( 3 )
	);	
	
	SignalEvent(OBJECT_SELF, EventSpellCastAt(OBJECT_SELF, END_DEFENSIVE_STANCE, FALSE));
	
	//RemoveEffect( OBJECT_SELF, eff );
}