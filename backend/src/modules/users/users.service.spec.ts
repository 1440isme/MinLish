import { UsersService } from './users.service';

describe('UsersService', () => {
  let service: UsersService;
  let prisma: any;
  let tx: any;

  beforeEach(() => {
    tx = {
      user: {
        create: jest.fn(),
      },
      notificationSetting: {
        create: jest.fn(),
      },
      deck: {
        create: jest.fn(),
      },
    };

    prisma = {
      $transaction: jest.fn(async (callback: (client: any) => Promise<unknown>) =>
        callback(tx),
      ),
    };

    service = new UsersService(prisma);
  });

  it('creates a local user with notification settings and a default Favorites deck', async () => {
    tx.user.create.mockResolvedValue({
      id: 'user-1',
      email: 'tester@example.com',
      fullName: 'Tester',
      authProvider: 'LOCAL',
    });

    const result = await service.create({
      email: 'tester@example.com',
      passwordHash: 'hashed-password',
      fullName: 'Tester',
    });

    expect(tx.notificationSetting.create).toHaveBeenCalledWith({
      data: {
        userId: 'user-1',
      },
    });
    expect(tx.deck.create).toHaveBeenCalledWith({
      data: expect.objectContaining({
        ownerUserId: 'user-1',
        deckType: 'USER',
        visibility: 'PRIVATE',
        name: 'Favorites',
        normalizedName: 'favorites',
        isDefault: true,
        totalWords: 0,
      }),
    });
    expect(result.id).toBe('user-1');
  });

  it('creates a Google user with the same default Favorites deck invariant', async () => {
    tx.user.create.mockResolvedValue({
      id: 'user-google-1',
      email: 'google@example.com',
      fullName: 'Google Tester',
      authProvider: 'GOOGLE',
      providerId: 'google-provider-1',
    });

    const result = await service.createGoogleUser({
      email: 'google@example.com',
      fullName: 'Google Tester',
      providerId: 'google-provider-1',
    });

    expect(tx.deck.create).toHaveBeenCalledWith({
      data: expect.objectContaining({
        ownerUserId: 'user-google-1',
        deckType: 'USER',
        name: 'Favorites',
        normalizedName: 'favorites',
        isDefault: true,
      }),
    });
    expect(result.id).toBe('user-google-1');
  });
});
