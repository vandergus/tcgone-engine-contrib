package tcgwars.logic.impl.gen7;

import static tcgwars.logic.card.HP.*;
import static tcgwars.logic.card.Type.*;
import static tcgwars.logic.card.CardType.*;
import static tcgwars.logic.groovy.TcgBuilders.*;
import static tcgwars.logic.groovy.TcgStatics.*
import static tcgwars.logic.effect.ability.Ability.ActivationReason.*
import static tcgwars.logic.effect.EffectType.*;
import static tcgwars.logic.effect.Source.*;
import static tcgwars.logic.effect.EffectPriority.*
import static tcgwars.logic.effect.special.SpecialConditionType.*
import static tcgwars.logic.card.Resistance.ResistanceType.*

import java.util.*;
import tcgwars.entity.*;
import tcgwars.logic.*;
import tcgwars.logic.card.*;
import tcgwars.logic.card.energy.*;
import tcgwars.logic.card.pokemon.*;
import tcgwars.logic.card.trainer.*;
import tcgwars.logic.effect.*;
import tcgwars.logic.effect.ability.*;
import tcgwars.logic.effect.ability.Ability.*;
import tcgwars.logic.effect.advanced.*;
import tcgwars.logic.effect.basic.*;
import tcgwars.logic.effect.blocking.*;
import tcgwars.logic.effect.event.*;
import tcgwars.logic.effect.getter.*;
import tcgwars.logic.effect.special.*;
import tcgwars.logic.util.*;

/**
 * @author axpendix@hotmail.com
 */
public enum UnbrokenBonds implements CardInfo {

  PHEROMOSA_BUZZWOLE_GX_1 ("Pheromosa & Buzzwole-GX", 1, Rarity.ULTRARARE, [POKEMON, BASIC, POKEMON_GX, TAG_TEAM, ULTRA_BEAST, _GRASS_]),
  
  static Type C = COLORLESS, R = FIRE, F = FIGHTING, G = GRASS, W = WATER, P = PSYCHIC, L = LIGHTNING, M = METAL, D = DARKNESS, Y = FAIRY, N = DRAGON;

  protected CardTypeSet cardTypes;
  protected String name;
  protected Rarity rarity;
  protected int collectionLineNo;

  UnbrokenBonds(String name, int collectionLineNo, Rarity rarity, List<CardType> cardTypes) {
    this.cardTypes = new CardTypeSet(cardTypes as CardType[]);
    this.name = name;
    this.rarity = rarity;
    this.collectionLineNo = collectionLineNo;
  }

  @Override
  public CardTypeSet getCardTypes() {
    return cardTypes;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Rarity getRarity() {
    return rarity;
  }

  @Override
  public int getCollectionLineNo() {
    return collectionLineNo;
  }

  @Override
  public tcgwars.logic.card.Collection getCollection() {
    return tcgwars.logic.card.Collection.UNBROKEN_BONDS;
  }

  @Override
  public String toString() {
    return String.format("%s:%s", this.name(), this.getCollection().name());
  }

  @Override
  public String getEnumName() {
    return name();
  }

  @Override
  public Card getImplementation() {
    switch (this) {
      case BLIZZARD_TOWN_187:
        return stadium (this) {
          text "Pokémon with 40 HP or less remaining (both yours and your opponent’s) can’t attack."
          def eff
          onPlay {
            eff = delayed {
              before CHECK_ATTACK_REQUIREMENTS, {
                if(ef.attacker.hp <= 40) {
                  wcu "Blizzard Town prevents attack"
                  prevent()
                }
              }
            }
          }
          onRemoveFromPlay{
            eff.unregister()
          }
        };
      case BLUE_S_TACTICS_188:
        return supporter (this) {
          text "At the end of this turn, draw cards until you have 8 cards in your hand."
          onPlay {reason->
            delayed {
              unregisterAfter 1
              unregister {draw (8 - hand.getExcludedList(thisCard).size())}
            }
          }
          playRequirement{
          
          }
        };
      case BUG_CATCHER_189:
        return supporter (this) {
          text "Draw 2 cards. Flip a coin. If heads, draw 2 more cards."
          onPlay {
            draw 2
            flip 1, {draw 2}
          }
          playRequirement{
            assert my.deck
          }
        };
      case CHERISH_BALL_191:
        return itemCard (this) {
          text "Search your deck for a Pokémon-GX, reveal it, and put it into your hand. Then, shuffle your deck."
          onPlay {
            deck.search("Search your deck for a Pokémon-GX",{it.cardTypes.pokemon && it.cardTypes.isIn(POKEMON_GX)}).moveTo(hand)
            shuffleDeck()
          }
          playRequirement{
            assert my.deck
          }
        };
      case COACH_TRAINER_192:
        return supporter (this) {
          text "Draw 2 cards. If your Active Pokémon is a TAG TEAM Pokémon, draw 2 more cards."
          onPlay {
            draw 2
            if(my.active.cardTypes.isIn(TAG_TEAM)) {draw 2}
          }
          playRequirement{
            assert my.deck
          }
        };
      case DARK_CITY_193:
        return stadium (this) {
          text "Basic [D] Pokémon in play (both your and your opponent’s) have no Retreat Cost."
          def eff
          onPlay {
            eff = getter (GET_RETREAT_COST) {Holder h->
              def pcs = h.effect.target
              if(pcs.types.contains(D) && pcs.basic){
                h.object = 0
              }
            }
          }
          onRemoveFromPlay{
            eff.unregister()
          }
        };
      case EAR_RINGING_BELL_194:
        return pokemonTool (this) {
          text "If the Pokémon this card is attached to is your Active Pokémon and is damaged by an opponent’s attack (even if that Pokémon is Knocked Out), the Attacking Pokémon is now Confused."
          def eff
          onPlay {reason->
            eff = delayed(priority: LAST) {
              before APPLY_ATTACK_DAMAGES, {
                bg().dm().each {
                  if (it.to == self && it.dmg.value > 0 && bg.currentTurn==self.owner.opposite
                    && self.active) {
                    bc "Ear-Ringing Bell activates"
                    apply CONFUSED, it.from, SRC_ABILITY
                  }
                }
              }
            }
          }
          onRemoveFromPlay {
            eff.unregister()
          }
        };
      case GIANT_BOMB_196:
        return pokemonTool (this) {
          text "If this card is attached to 1 of your Pokémon, discard it at the end of your opponent’s turn." +
            "If the Pokémon this card is attached to is your Active Pokémon and takes 180 or more damage from an opponent’s attack (even if this Pokémon is Knocked Out), put 10 damage counters on the Attacking Pokémon."
          def eff
          onPlay {reason->
            eff=delayed (priority: LAST){
              before APPLY_ATTACK_DAMAGES,{
                if(bg.currentTurn == self.owner.opposite && bg.dm().find({it.to==self && it.dmg.value >= 180}) && self.active){
                  directDamage(100, ef.attacker, TRAINER_CARD)
                  bc "Giant Bomb explodes"
                }
              }
              unregister {discard thisCard}
              unregisterAfter 2
            }
          }
          onRemoveFromPlay {
            eff.unregister()
          }
        };
      case GIANT_HEARTH_197:
        return stadium(this) {
          text "Once during each player’s turn, that player may discard a card from their hand. If they do, that player searches their deck for up to 2 [R] Energy cards, reveals them, and puts them into their hand. Then, that player shuffles their deck."
          def lastTurn=0
          def actions=[]
          onPlay {
            actions=action("Stadium: Viridian Forest") {
              assert my.deck : "There are no more cards in your deck"
              assert my.hand : "You don't have cards in your hand"
              assert lastTurn != bg().turnCount : "Already used"
              bc "Used Giant Hearth's effect"
              lastTurn = bg().turnCount
              my.hand.select("Choose the card to discard").discard()
              my.deck.search(max:2,"Select up to 2 Fire Energy cards",energyFilter(R)).showToOpponent("The selected Fire Energy cards").moveTo(my.hand)
              shuffleDeck()
            }
          }
          onRemoveFromPlay{
            actions.each { bg().gm().unregisterAction(it) }
          }
        };
      case GREAT_POTION_198:
        return itemCard (this) {
          text "Heal 50 damage from your Active Pokémon-GX."
          onPlay {
            heal 50, my.active
          }
          playRequirement{
            assert my.active.cardTypes.isIn(POKEMON_GX) : "Active Pokemon is no"
            assert my.active.numberOfDamageCounters : "There is no damage to heal"
          }
        };
      case GRIMSLEY_199:
        return supporter (this) {
          text "Move up to 3 damage counters from 1 of your opponent’s Pokémon to another of their Pokémon."
          onPlay {
            def pcs = opp.all.findAll {it.numberOfDamageCounters}.select("Move damage counters from")
            def tar = opp.all.findAll {it != pcs}.select("To?")
            def num = Math.min(3, pcs.numberOfDamageCounters)
            pcs.damage -= hp(10*num)
            tar.damage += hp(10*num)
            bc "Moved $num damage counters from $pcs to $tar"
          }
          playRequirement{
            assert opp.all.size() >= 2 : "Opponent only has one Pokemon in play"
            assert opp.all.findAll {it.numberOfDamageCounters} : "There are no damage counters to move"
          }
        };
      case HAPU_200:
        return supporter (this) {
          text "Look at the top 6 cards of your deck and put 2 of them into your hand. Discard the other cards."
          onPlay {
            def cards = my.deck.subList(0,6)
            cards.select(count:2,"Choose 2 cards to put in your hand").moveTo(my.hand)
            cards.discard
          }
          playRequirement{
            assert my.deck : "There are no cards in your deck"
          }
        };
      case KARATE_BELT_201:
        return pokemonTool (this) {
          text "If you have more Prize cards remaining than your opponent, the attacks of the Pokémon this card is attached to cost [F] less."
          def eff1
          onPlay {reason->
            eff1=getter GET_MOVE_LIST, self, {h->
              if(self.owner.pbg.prizeCardSet.size() > self.owner.opposite.pbg.prizeCardSet.size()){
                def list=[]
                for(move in h.object){
                  def copy=move.shallowCopy()
                  copy.energyCost.remove(F)
                  list.add(copy)
                }
                h.object=list
              }
            }
          }
          onRemoveFromPlay {
            eff1.unregister()
          }
        };
      case MISTY_S_FAVOR_202:
        return supporter (this) {
          text "Search your deck for up to 3 Supporter cards, reveal them, and put them into your hand. Then, shuffle your deck."
          onPlay {
            deck.search(max:3,"Select up to 3 Supporter cards",cardTypeFilter(SUPPORTER)).moveTo(hand)
            shuffleDeck()
          }
          playRequirement{
            assert my.deck : "There are no cards in your deck"
          }
        };
      case POKE_MANIAC_204:
        return supporter (this) {
          text "Search your deck for up to 3 Pokémon that have a Retreat Cost of exactly 4, reveal them, and put them into your hand. Then, shuffle your deck."
          onPlay {
            deck.search(max:3,"Select up to 3 Pokemon with a Retreat Cost of 4",{it.cardTypes.is(POKEMON) && it.retreatCost == 4}).moveTo(hand)
            shuffleDeck()
          }
          playRequirement{
            assert my.deck : "There are no cards in your deck"
          }
        };
        
        default:
        return null;
    }
  }

}
