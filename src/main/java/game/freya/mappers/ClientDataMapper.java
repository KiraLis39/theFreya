package game.freya.mappers;

import game.freya.dto.PlayCharacterDto;
import game.freya.dto.roots.CharacterDto;
import game.freya.dto.roots.PlayerDto;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.net.data.ClientDataDto;
import game.freya.net.data.events.EventHeroRegister;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public final class ClientDataMapper {

    public ClientDataDto playCharacterDtoToClientData(CharacterDto character, PlayerDto player) {
        if (character == null) {
            return null;
        }

        return ClientDataDto.builder()
                .dataType(NetDataType.EVENT)
                .dataEvent(NetDataEvent.HERO_REGISTER)
                .content(EventHeroRegister.builder()
                        .ownerUid(player.getUid())
                        .playerName(player.getNickName())
                        .heroUid(character.getUid())
                        .heroName(character.getName())
                        .heroType(character.getType())
                        .baseColor(character.getBaseColor())
                        .secondColor(character.getSecondColor())
                        .corpusType(character.getCorpusType())
                        .peripheryType(character.getPeripheralType())
                        .peripherySize(character.getPeripheralSize())
                        .level(character.getLevel())
                        .hp(character.getHealth())
                        .maxHp(character.getMaxHealth())
                        .oil(character.getOil())
                        .maxOil(character.getMaxOil())
                        .speed(character.getSpeed())
                        .vector(character.getVector())
                        .positionX(character.getLocation().x)
                        .positionY(character.getLocation().y)
                        .worldUid(character.getWorldUid()).build())
                .power(character.getPower())
                .experience(character.getExperience())
//                .buffs(hero.getBuffs())
//                .inventory(hero.getInventory())
//                .inGameTime(hero.inGameTime())
                .build();
    }

    public PlayCharacterDto clientDataToPlayCharacterDto(ClientDataDto cli) {
        if (cli == null || cli.content() == null) {
            return null;
        }

        EventHeroRegister heroRegister = (EventHeroRegister) cli.content();
        return PlayCharacterDto.builder()
                .type(heroRegister.heroType())

                .baseColor(heroRegister.baseColor())
                .secondColor(heroRegister.secondColor())
                .corpusType(heroRegister.corpusType())
                .peripheralType(heroRegister.peripheryType())
                .peripheralSize(heroRegister.peripherySize())

                .worldUid(heroRegister.worldUid())

//                .power(heroRegister.power())
//                .experience(heroRegister.experience())
//                .inGameTime(heroRegister.inGameTime())
//                .lastPlayDate(heroRegister.lastPlayDate())
//                .createdDate(heroRegister.createdDate())

//                .inventory(heroRegister.getInventory())
//                .buffs(heroRegister.getBuffs())

//                .setAuthor(heroRegister.playerUid())
//                .setUid(heroRegister.heroUid())
//                .setName(heroRegister.heroName())
//                .setSpeed(heroRegister.speed())
//                .setLocation(heroRegister.positionX(), heroRegister.positionY())
//                .setHealth(heroRegister.hp())
//                .setMaxHealth(heroRegister.maxHp())
//                .setOil(heroRegister.oil())
//                .setMaxOil(heroRegister.maxOil())
//                .setLevel(heroRegister.level())
                .build();
    }
}
