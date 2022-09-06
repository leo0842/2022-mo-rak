import { useTheme } from '@emotion/react';
import styled from '@emotion/styled';
import TextField from '../../@common/TextField/TextField';

interface Props {
  isClosed: boolean;
}

function AppointmentMainStatus({ isClosed }: Props) {
  const theme = useTheme();

  return (
    <TextField
      variant="filled"
      width="4rem"
      padding="0.4rem 0"
      borderRadius="5px"
      colorScheme={!isClosed ? theme.colors.PURPLE_100 : theme.colors.GRAY_400}
    >
      <StyledStatus>{!isClosed ? '진행중' : '완료'}</StyledStatus>
    </TextField>
  );
}

const StyledStatus = styled.span(
  ({ theme }) => `
  color: ${theme.colors.WHITE_100};
`
);

export default AppointmentMainStatus;
