import { Dispatch, SetStateAction, MouseEvent, ChangeEvent, memo } from 'react';

import { useTheme } from '@emotion/react';

import { StyledDeleteIcon } from './PollCreateFormInputs.styles';
import FlexContainer from '../../../../components/FlexContainer/FlexContainer';
import Input from '../../../../components/Input/Input';
import TextField from '../../../../components/TextField/TextField';
import Button from '../../../../components/Button/Button';

import binImg from '../../../../assets/bin.svg';
import { PollItem } from '../../../../types/poll';

type Props = {
  // TODO: pollItems 괜찮을까? subjects가 아닐까?
  pollItems: Array<PollItem['subject']>;
  setPollItems: Dispatch<SetStateAction<Array<PollItem['subject']>>>;
};

function PollCreateFormInputs({ pollItems, setPollItems }: Props) {
  const theme = useTheme();

  const handleAddPollItem = (e: MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();

    if (pollItems.length >= 10) {
      alert('최대 10개의 선택항목만 가능합니다');

      return;
    }

    setPollItems([...pollItems, '']);
  };

  const handleDeletePollItem = (targetIdx: number) => () => {
    // TODO: 상수화
    if (window.confirm('해당 항목을 삭제하시겠습니까?')) {
      if (pollItems.length === 2) {
        alert('선택항목은 최소 2개이상이여야합니다.');
        return;
      }

      const newPollItems = [...pollItems].filter((_, idx) => idx !== targetIdx);

      setPollItems(newPollItems);
    }
  };

  const handleChange = (targetIdx: number) => (e: ChangeEvent<HTMLInputElement>) => {
    const newPollItems = [...pollItems];

    newPollItems[targetIdx] = e.target.value;

    setPollItems(newPollItems);
  };

  return (
    <FlexContainer flexDirection="column" gap="1.2rem">
      {pollItems.map((pollItem, idx) => (
        // TODO: key를 넣어줘야한다.
        // eslint-disable-next-line react/jsx-key
        <TextField
          variant="outlined"
          borderRadius="10px"
          padding="1.2rem 10rem"
          position="relative"
          colorScheme={theme.colors.PURPLE_100}
        >
          <FlexContainer alignItems="center">
            <Input
              id={pollItem}
              value={pollItem}
              color={theme.colors.BLACK_100}
              fontSize="1.6rem"
              placeholder="선택항목을 입력해주세요!"
              onChange={handleChange(idx)}
              aria-label={`poll-input${idx}`}
              required
            />
            <StyledDeleteIcon src={binImg} alt="bin" onClick={handleDeletePollItem(idx)} />
          </FlexContainer>
        </TextField>
      ))}
      <Button
        variant="filled"
        colorScheme={theme.colors.PURPLE_100}
        fontSize="2rem"
        onClick={handleAddPollItem}
        type="button"
      >
        +
      </Button>
    </FlexContainer>
  );
}

export default memo(PollCreateFormInputs, (prev, next) => prev.pollItems === next.pollItems);
