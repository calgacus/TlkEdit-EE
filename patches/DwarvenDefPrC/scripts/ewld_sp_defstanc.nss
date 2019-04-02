#include "patchdefinitions"

/*
Defensive Stance:  When he adopts a defensive stance, a defender gains phenomenal strength and durability, but he cannot move from the spot he is defending. He gains +2 to Strength, +4 to Constitution, a +2 resistance bonus on all saves, and a +4 dodge bonus to AC. The increase in Constitution increases the defender's hit points by 2 points per level, but these hit points go away at the end of the defensive stance when the Constitution score drops back 4 points. These extra hit points are not lost first the way temporary hit points are. While in a defensive stance, a defender cannot use skills or abilities that would require him to shift his position. A defensive stance lasts for a number of rounds equal to 3 + the character's (newly improved) Constitution modifier. A defender may end his defensive stance voluntarily prior to this limit. At the end of the defensive stance, the defender is winded and takes a -2 penalty to Strength for the duration of that encounter. A defender can only use his defensive stance a certain number of times per day as determined by his level (see Table: The Dwarven Defender). Using the defensive stance takes no time itself, but a defender can only do so during his action.
*/

void main(){
/*
	increase STR by 4 and decrease by 2, decrease will last 3 rounds longer
	than stance ( to simulate the 'winded' effect )
*/
	effect eSTR   = EffectAbilityIncrease( ABILITY_STRENGTH,     4 );
	effect eWINDED= ExtraordinaryEffect( EffectAbilityDecrease( ABILITY_STRENGTH,     2 ) );
	
	effect eCON   = EffectAbilityIncrease( ABILITY_CONSTITUTION, 4 );
	effect eDODGE = EffectACIncrease( 4, AC_DODGE_BONUS, AC_VS_DAMAGE_TYPE_ALL );
	effect eSAV   = EffectSavingThrowIncrease( SAVING_THROW_ALL, 2, SAVING_THROW_TYPE_ALL );
	//effect eIMOB  = EffectMovementSpeedDecrease( 99 );
	effect eIMOB;
	if ( GetLevelByClass( CLASS_TYPE_DWARVENDEFENDER ) < 8 ){
		eIMOB = EffectMovementSpeedDecrease( 99 );
		/*
		eIMOB = EffectLinkEffects(
			EffectEntangle(),
			EffectLinkEffects(
				EffectACIncrease( 4, AC_DODGE_BONUS, AC_VS_DAMAGE_TYPE_ALL ),
				EffectAttackIncrease( 2 )
				)
			);
		*/
	}
	else{ // mobile defense
		eIMOB = EffectMovementSpeedDecrease( 99 );
	}
	effect eff =
		EffectLinkEffects( eIMOB,
			EffectLinkEffects(
				EffectLinkEffects( eSTR, eCON ), EffectLinkEffects( eDODGE, eSAV )
			)
		);
	eff = ExtraordinaryEffect( eff );
	
	// uncrease duration by 1 round because activation requires a full round
	// duration = 3 + 2 (4 con bonus) + 1 + <constitution modifier>
	float dur = RoundsToSeconds(
		6 + GetAbilityModifier( ABILITY_CONSTITUTION, OBJECT_SELF )
		);
	float durWinded = dur + RoundsToSeconds( 3 );	
	
	SignalEvent(OBJECT_SELF, EventSpellCastAt(OBJECT_SELF, DEFENSIVE_STANCE, FALSE));
	
	effect eVis = EffectVisualEffect(VFX_IMP_IMPROVE_ABILITY_SCORE);
	
	ApplyEffectToObject(
		DURATION_TYPE_TEMPORARY,
		eff,
		OBJECT_SELF,
		dur	
	);
	
	ApplyEffectToObject(
		DURATION_TYPE_TEMPORARY,
		eWINDED,
		OBJECT_SELF,
		durWinded
	);
	
	ApplyEffectToObject(DURATION_TYPE_INSTANT, eVis, OBJECT_SELF);
	
}
